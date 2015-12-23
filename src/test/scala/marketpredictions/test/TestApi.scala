package marketpredictions.test

import java.sql.Timestamp;
import marketpredictions.engine._

import slick.driver.H2Driver.api._

case class TestApi(override val db: Database, 
                  override val now: Timestamp) extends Api
                                      with SimpleEngineContext
{

/*
  def withClockAndPrediction(when:Timestamp,eventId: Long, alternative: Int):DBIO[
  {
   var prevNow = now;
   now = when;
   markPredictionResult(eventId, alternative);
  }
*/

}



// vim: set ts=4 sw=4 et:
