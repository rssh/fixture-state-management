package marketpredictions.db

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;


object MPSchema extends Schema
{

 val predictions = table[Prediction];

 val members = table[Member];

 val authority = oneToManyRelation(members,predictions).via( (m,p) => m.id===p.authorId);

 val bids = manyToManyRelation(members,predictions).via[Bid] ( 
                (m, p, mp) => (m.id === mp.memberId, p.id === mp.predictionId)
            );



  def postInit: Unit = 
  {
    Prediction.schemaInit;
  }

  def preClear: Unit = 
  {
    Prediction.schemaClear;
  }


}

// vim: set ts=4 sw=4 et:
