package marketpredictions.test

import java.sql.Timestamp;
import marketpredictions.engine._

import slick.driver.H2Driver.api._

class TestApi(override val db: Database, 
              override var now: Timestamp) extends Api
                                      with SimpleEngineContext
{


  def setClockAndPrediction(when:Timestamp,eventId: Long, alternative: Int)
  {
   var prevNow = now;
   now = when;
   markPredictionResult(eventId, alternative);
  }

}

object TestApi
{
   def apply(db: Database, now: Timestamp) = new TestApi(db,now)

}


// vim: set ts=4 sw=4 et:
