package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import luqman.sahaf.playlivy.LivyManager
import org.apache.spark.sql.SparkSession
import play.api.mvc._
import play.api.Configuration
import play.Logger
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}


class Application @Inject() (val system: ActorSystem, config: Configuration, livyManager: LivyManager)
  extends Controller {

  /**
    * @return Action returning Ok
    * */
  def index = Action {
    Ok("Sample Project for Play Livy Module")
  }

  /**
    * Runs a Pi calculation example on Spark via Livy Play Module (play-livy)
    * The request will wait 60 seconds for the job on Livy/Spark to finish.
    *
    * @param num String assumed to be Integer
    *
    * @return Action containing Result Ok and Pi Calculation in data, otherwise error in Status.
    * */
  def runPiExample(num: String) = Action {
    val number = num.toInt
    try {
      val handle = livyManager.submit(context => {
        import scala.math.{random,max,min}
        val spark: SparkSession = context.sparkSession
        // Pi calculation code
        // Adapted from: https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/SparkPi.scala
        val slices = max(number,2)
        val n = min(100000L * slices, Int.MaxValue).toInt // avoid overflow
        val count = spark.sparkContext.parallelize(1 until n, slices).map { i =>
            val x = random * 2 - 1
            val y = random * 2 - 1
            if (x*x + y*y <= 1) 1 else 0
          }.reduce(_ + _)
        println("Pi is roughly " + 4.0 * count / (n - 1))
        4.0 * count / (n - 1)
      })
      Await.ready(handle, 60 second)
      handle.value match {
        case Some(Success(pi)) =>
          Logger.info(s"pi = $pi")
          Ok(pi.toString)
        case Some(Failure(e)) =>
          Logger.error(s"error occurred while computing pi: ${e.getMessage}")
          e.printStackTrace()
          Status(500)
        case None => Status(500)
      }
    } catch {
      case e: Exception =>
        Logger.error("error occurred")
        e.printStackTrace()
        Status(500)
    }
  }
}