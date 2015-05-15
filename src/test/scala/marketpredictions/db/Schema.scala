package marketpredictions.db

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


object MPSchema 
{

 //val predictedEvents = table[PredictedEvent];
 //on(predictedEvents)(e => declare(e.idname is(unique,indexed)));

 val members = TableQuery[Members]

 //val authority = oneToManyRelation(members,predictedEvents).via( (m,p) => m.id===p.authorId);

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
