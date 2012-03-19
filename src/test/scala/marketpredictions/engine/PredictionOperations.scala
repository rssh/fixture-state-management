package marketpredictions.engine

import java.sql.Timestamp;

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;

import marketpredictions.db._
import marketpredictions.db.MPSchema._



trait PredictionOperations
{

  this: Api with EngineContext =>

  def createPrediction(userId: Long,
                       description: String,
                       nAlternatives: Int,
                       dateToCheck: Timestamp,
                       minSum: BigDecimal=BigDecimal(0L)):Long =
  {
     if (dateToCheck.before(now)) {
       throw new IllegalArgumentException("dateToCheck before now");
     }
     if (nAlternatives <= 0) {
       throw new IllegalArgumentException("nAlternatives <= 0");
     }
     val newPrediction = predictions insert Prediction(
                                              id = -1L, // will  be generated
                                              description = description,
                                              nAlternatives = nAlternatives,
                                              passTime=dateToCheck,
                                              startBidding=now,
                                              stopBidding=dateToCheck,
                                              actualSum=BigDecimal(0L),
                                              minSum=minSum,
                                              closed=false,
                                              result=None,
                                              authorId=userId
                                            );
     newPrediction.id;
  }
                        

  def bid(userId: Long,
          predictionId: Long,
          alternative: Int,
          sum: BigDecimal) : Bid =
  {
   val u = members.lookup(userId).get
   val p = predictions.lookup(predictionId).get
   if (sum <= 0) {
       throw new IllegalArgumentException("sum too small");
   }
   if (alternative <0 || alternative >= p.nAlternatives) {
       throw new IllegalArgumentException("invalid alternative");
   }
   if (u.balance < sum) {
       throw new IllegalArgumentException("too small balance");
   }
   if (p.closed) {
       throw new IllegalArgumentException("prediction is closed");
   }
   if (p.stopBidding.before(now)) {
       throw new IllegalArgumentException("bidding is stopped");
   }
   // are we already have bid for this person ?
   members update u.copy(balance = u.balance - sum);
   predictions update p.copy(actualSum = p.actualSum+sum);
   bids.lookup(CompositeKey2(userId,predictionId)) match {
     case Some(bid) =>
       val newBid = bid.copy(sum=bid.sum+sum,when=now)
       bids update newBid
       newBid
     case None =>
       bids insert new Bid(userId,predictionId,alternative,sum,now);
   } 
  }
          
  def markPredictionResult(predictionId: Long,
                           alternative: Int): Unit =
  {
   val p = predictions.lookup(predictionId).get
   if (p.closed) {
     throw new IllegalArgumentException("p is already closed");
   }

  }
                           

}

// vim: set ts=4 sw=4 et:
