package luqman.sahaf.playlivy

import LivyModuleLogger.logger

/**
  * @author Luqman.
  *
  * RefreshJob extends Runnable. It is used to schedule a job to refresh Livy Session
  * to keep it alive.
  */
class RefreshJob extends Runnable {
  private var manager: LivyManager = null
  def this(m: LivyManager) {
    this
    manager = m
  }

  override def run(): Unit = {
    try {
      logger.trace("Submitting refresh job.")
      manager.submit( context => {
        1+1
      })
    } catch {
      case e: Exception=> logger.error("Problem in refreshing Livy Scala Session", e);
    }
  }
}
