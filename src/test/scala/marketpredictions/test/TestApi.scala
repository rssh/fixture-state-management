package marketpredictions.test

import java.sql.Timestamp;
import marketpredictions.engine._

class TestApi(override var now: Timestamp) extends Api
                                   with SimpleEngineContext
{


  def setClockAndPrediction(when:Timestamp,eventId: Long, alternative: Int)
  {
   var prevNow = now;
   now = when;
   markPredictionResult(eventId, alternative);
  }


}


// vim: set ts=4 sw=4 et:
