package marketpredictions.engine

import java.sql.Timestamp
import java.sql.Connection

/**
  * services, which used by engine.
  */
trait EngineContext
{

   def now: Timestamp

   def sqlConnection: Connection


}

// vim: set ts=4 sw=4 et:
