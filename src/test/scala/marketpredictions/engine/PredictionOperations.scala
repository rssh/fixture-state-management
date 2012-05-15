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

  def createEvent(userId: Long,
                  description: String,
                  nAlternatives: Int,
                  dateToCheck: Timestamp,
                  minSum: BigDecimal=BigDecimal(0L)):Long =
  inTransaction {
     if (dateToCheck.before(now)) {
       throw new IllegalArgumentException("dateToCheck before now");
     }
     if (nAlternatives <= 1) {
       throw new IllegalArgumentException("nAlternatives <= 1");
     }
     val newEvent = predictedEvents insert PredictedEvent(
                                              id = -1L, // will  be generated
                                              description = description,
                                              nAlternatives = nAlternatives,
                                              passTime=dateToCheck,
                                              startBidding=now,
                                              stopBidding=dateToCheck,
                                              actualSum=BigDecimal(0L),
                                              minSum=minSum,
                                              closed=false,
                                              failed=None,
                                              result=None,
                                              authorId=userId
                                            );
     newEvent.id;
  }
                        

  def bid(userId: Long,
          eventId: Long,
          alternative: Int,
          sum: BigDecimal) : Bid =
  {
   val u = members.lookup(userId).get
   val p = predictedEvents.lookup(eventId).get
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
   predictedEvents update p.copy(actualSum = p.actualSum+sum);
   bids.lookup(CompositeKey2(userId,eventId)) match {
     case Some(bid) =>
       val newBid = bid.copy(sum=bid.sum+sum,when=now)
       bids update newBid
       newBid
     case None =>
       bids insert new Bid(userId,eventId,alternative,sum,now);
   } 
  }
          
  def markPredictionResult(eventId: Long,
                           alternative: Int): Unit =
  inTransaction {
   val p = predictedEvents.lookup(eventId).get
   if (p.closed) {
     throw new IllegalArgumentException("p is already closed");
   }
   var failed:Boolean=true;
   if (p.actualSum < p.minSum) {
     returnBids(p);
   }
   val bids = p.bids.toList;
   if (bids.isEmpty) {
     // do nothing, no bids has been bidded.
   } else if (bids.length==1) {
     // only one member, so return 
     returnBids(p);
   } else {
     val byAlternatives = bids.groupBy(_.alternative);
     byAlternatives.get(alternative) match {
        case None => returnBids(p);
        case Some(rightBids) => 
                   val rightSum = rightBids.map(_.sum).sum;
                   val allSum = bids.map(_.sum).sum;
                   val up = allSum*(1.0-organizerComission);
                   val down = rightSum;
                   for(bid <- bids) {
                     if (bid.alternative == alternative) {
                       // add to member 
                       for( m <- members.lookup(bid.memberId) ) {
                          val gain = (bid.sum * up / down).setScale(2,BigDecimal.RoundingMode.HALF_EVEN);
                          members update m.copy(balance = m.balance+gain)
                       }
                     }
                   }
                   failed=false;
     }
   }
   predictedEvents update p.copy(closed=true, failed=Some(failed), result=Some(alternative))
  }
                           
  private def returnBids(p:PredictedEvent): Unit = 
  inTransaction {
    // return all money to bid authors.
    for(bid <- p.bids) {
       update(members)(m => where(m.id === bid.memberId)
                           set( m.balance := m.balance + bid.sum )
                     );
    }
  }

  def organizerComission = 0.01;

}

// vim: set ts=4 sw=4 et:
