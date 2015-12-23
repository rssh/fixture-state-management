package marketpredictions.engine

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import java.sql._;


trait SimpleEngineContext extends EngineContext
{

   def onInit: Unit =
   {
   }

   def onShutdown: Unit = {}

   def now = new Timestamp(System.currentTimeMillis);

   def now_=(x: Timestamp):Unit = throw new UnsupportedOperationException();

}

// vim: set ts=4 sw=4 et:
