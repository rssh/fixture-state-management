package marketpredictions.db

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


object MPSchema 
{


 val members = TableQuery[Members]

 val predictedEvents = TableQuery[PredictedEvents]

 val bids = TableQuery[Bids]

  def schema = (members.schema ++ predictedEvents.schema ++ bids.schema)

  def create: DBIO[Unit] =
    schema.create

  def drop: DBIO[Unit] =
    schema.drop

  def postInit: DBIO[Unit] = 
  {
    DBIO.successful(())
    //PredictedEvent.schemaInit;
  }

  def preClear: DBIO[Unit] = 
  {
    DBIO.successful(())
    //PredictedEvent.schemaClear;
  }


}

// vim: set ts=4 sw=4 et:
