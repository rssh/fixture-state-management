package marketpredictions.db

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;


object MPSchema extends Schema
{

 val predictedEvents = table[PredictedEvent];
 on(predictedEvents)(e => declare(e.idname is(unique,indexed)));

 val members = table[Member];

 val authority = oneToManyRelation(members,predictedEvents).via( (m,p) => m.id===p.authorId);

 val bids = manyToManyRelation(members,predictedEvents).via[Bid] ( 
                (m, p, mp) => (m.id === mp.memberId, p.id === mp.eventId)
            );



  def postInit: Unit = 
  {
    PredictedEvent.schemaInit;
  }

  def preClear: Unit = 
  {
    PredictedEvent.schemaClear;
  }


}

// vim: set ts=4 sw=4 et:
