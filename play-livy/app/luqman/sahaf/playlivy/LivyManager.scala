package luqman.sahaf.playlivy

import akka.actor.ActorSystem
import com.cloudera.livy.LivyClientBuilder
import com.cloudera.livy.scalaapi._
import javax.inject.Inject
import java.io.{File, FileNotFoundException}
import java.net.URI
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.collection.JavaConverters._
import scala.language.postfixOps
import LivyModuleLogger.logger

/**
  * @author Luqman.
  *
  * LivyManager can be used to manage remote Livy Sessions on Livy and YARN clusters to run Spark code,
  * written in Scala interactively. It can start/stop Livy sessions, submit code to Livy Session and
  * return result. You can also upload Jar files used in your code to Livy session via uploadJar function
  *
  * @param system ActorSystem to run Refresh Jobs on specific intervals to keep Livy Session
  *              alive.
  *
  * @param lifecycle to register onStop events to shut down remote Livy session
  *
  * @param config to read configurations provided by user to control different aspects of initialization
  *
  */
class LivyManager @Inject()(system: ActorSystem, lifecycle: ApplicationLifecycle, config: Configuration) extends LivyManagement {
  implicit val dispatcher = system.dispatcher
  init()
  lifecycle.addStopHook { () =>
    stop()
    Future.successful(())
  }

  private lazy val scalaClient: LivyScalaClient = {
    val url = config.getString("livy.uri")
    url match {
      case Some(uri) =>
        var builder = new LivyClientBuilder(false)
          .setURI(new URI(uri))
          .setConf("spark.driver.memory", config.getString("spark.driver.memory").getOrElse("512m"))
          .setConf("spark.executor.memory", config.getString("spark.executor.memory").getOrElse("512m"))
          .setConf("spark.driver.cores", config.getInt("spark.driver.cores").getOrElse(1).toString)
          .setConf("spark.executor.cores", config.getInt("spark.executor.cores").getOrElse(1).toString)
          .setConf("spark.executor.instances", config.getInt("spark.executor.instances").getOrElse(2).toString)
        if (config.getString("spark.driver.extraClassPath").isDefined) {
          builder = builder.setConf("spark.driver.extraClassPath", config.getString("spark.driver.extraClassPath").get)
        }
        if (config.getString("spark.executor.extraClassPath").isDefined) {
          builder = builder.setConf("spark.executor.extraClassPath", config.getString("spark.executor.extraClassPath").get)
        }
        builder.build.asScalaClient
      case None => throw new Exception("Livy URI not found under \"livy.uri\" path")
    }
  }

  /**
    * Initializes the Livy Session, uploads relevant files to started session,
    * starts a sample job to validate remote session, and schedules a refresh
    * job, which will refresh the remote session every x seconds
    * */
  def init(): Unit = {
    try {
      scalaClient
      logger.info("Livy session created successfully.")

      if (config.getBoolean("livy.files.toUpload").getOrElse(false)) {
        logger.info("Uploading Jars to started Livy session.")
        val futureUploads = uploadRelevantJarsForJobExecution()
        Await.result(futureUploads, config.getInt("livy.files.wait").getOrElse(120) second)
        logger.debug("Uploaded files... Starting sample job")
      }

      val handle = submit( context => {
        1+1
      })
      Await.ready(handle, config.getInt("livy.wait").getOrElse(30) second)

      if (config.getBoolean("livy.refreshJob.start").getOrElse(false)) {
        logger.info("starting Refresh Job to refresh Livy Session after every x seconds")
        startRefreshJob()
      }
      logger.info("Livy module initialization finished")
    } catch {
      case e : Throwable =>
        logger.error("Error occurred while starting Livy Scala Client...")
        stop()
        throw e
    }
  }

  /**
    * schedules a [[RefreshJob]] to keep session alive
    * */
  def startRefreshJob(): Unit = {
    val job = new RefreshJob(this)
    system.scheduler.schedule(
      6 minute,
      config getInt "livy.refreshJob.interval" getOrElse 900 second,
      job
    )
  }

  /**
    * Stops the Livy Session, submitted jobs and Spark Session
    * */
  def stop() = scalaClient.stop(true)

  /**
    * Submit a job to Livy Session and get the result
    *
    * @param fn Anonymous function which takes ScalaJobContext and returns a value T
    *
    * @return ScalaJobHandle is a Future which will return result upon successful completion
    *         of job
    *
    * */
  def submit[T](fn: (ScalaJobContext) => T): ScalaJobHandle[T] = scalaClient.submit(fn)

  // Upload jars code follows
  // adapted from: https://github.com/cloudera/livy/blob/bfdb5a1925327758b054d8f67191e2bf4bc2c2bb/examples/src/main/scala/com/cloudera/livy/examples/WordCountApp.scala

  /**
    * Given a path of file uploads it to remote Livy Session
    *
    * @param path String path to file
    *
    * @return Future[_] for file upload
    * */
  def uploadFile(path: String) = {
    val file = new File(path)
    logger.debug(s"File: $path")
    scalaClient.uploadJar(file)
  }

  /**
    * Given an Object, returns path of Jar from where the code is taken for the object
    *
    * @param obj Object
    *
    * @return String Path of Jar file where the class/object is from.
    */
  @throws(classOf[FileNotFoundException])
  private def getSourcePath(obj: Object): String = {
    val source = obj.getClass.getProtectionDomain.getCodeSource
    if (source != null && source.getLocation.getPath != "") {
      source.getLocation.getPath
    } else {
      throw new FileNotFoundException(s"Jar containing ${obj.getClass.getName} not found.")
    }
  }

  /**
    * Uploads the Scala-API Jar and the jars mentioned in application config under "livy.files.list" path.
    *
    * @throws java.io.FileNotFoundException If either of Scala-API Jar or other jars are not found.
    */
  @throws(classOf[FileNotFoundException])
  private def uploadRelevantJarsForJobExecution() = {
    val jars_paths = getSourcePath(scalaClient) :: (config.getStringList("livy.files.list") map (_.asScala.toList) getOrElse Nil)
    val futureUploads = jars_paths map (f => uploadFile(f))
    Future.sequence(futureUploads)
  }

}
