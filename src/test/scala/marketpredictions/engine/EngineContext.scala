package marketpredictions.engine

import java.sql.Timestamp
//import slick.jdbc.JdbcBackend._
import slick.driver.H2Driver.api._


/**
  * services, which used by engine.
  */
trait EngineContext
{

   def onInit;

   def onShutdown;

   def now: Timestamp

   def db: Database

}

// vim: set ts=4 sw=4 et:
