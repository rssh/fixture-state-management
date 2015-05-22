package marketpredictions.db

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


object MPSchema 
{


 val members = TableQuery[Members]

 val predictedEvents = TableQuery[PredictedEvents]

 val bids = TableQuery[Bids]


  def postInit: Unit = 
  {
    //PredictedEvent.schemaInit;
  }

  def preClear: Unit = 
  {
    //PredictedEvent.schemaClear;
  }


}

// vim: set ts=4 sw=4 et:
