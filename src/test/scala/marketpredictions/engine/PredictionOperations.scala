package marketpredictions.engine

import java.sql.Timestamp;

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global



import marketpredictions.db._
import marketpredictions.db.MPSchema._



trait PredictionOperations
{

  this: Api with EngineContext =>

  def createEvent(userId: Long,
                  idname: String,
                  description: String,
                  nAlternatives: Int,
                  dateToCheck: Timestamp,
                  minSum: BigDecimal=BigDecimal(0L)): DBIO[Long] =
  {
     if (dateToCheck.before(now)) {
       throw new IllegalArgumentException("dateToCheck before now");
     }
     if (nAlternatives <= 1) {
       throw new IllegalArgumentException("nAlternatives <= 1");
     }
     (predictedEvents returning (predictedEvents map (_.id) )) +=
                                             PredictedEvent(
                                              id = None,
                                              idname = idname,
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
                                            )
  }
                        
  def findEvent(id:Long):DBIO[Option[PredictedEvent]] =
    predictedEvents.filter(_.id===id).result.headOption 

  def findEvent(idname:String):DBIO[Option[PredictedEvent]] =
    predictedEvents.filter(_.idname === idname).result.headOption

  def bid(userId: Long,
          eventId: Long,
          alternative: Int,
          sum: BigDecimal) : DBIO[Bid] =
  {
   if (sum <= 0) {
       throw new IllegalArgumentException("sum too small");
   }

   val bidq = bids.filter(bid => bid.memberId===userId && bid.eventId === eventId)

   def insertOrUpdateBids():DBIO[Bid] = 
        bidq.result.headOption.flatMap {
          case Some(bid) => val newBid = bid.copy(sum=bid.sum+sum,when=now)
                            (bidq update newBid) map (_ => newBid)
          case None => val newBid = Bid(userId,eventId,alternative,sum,now)
                            ((bids returning bids) += newBid)
        }

   def checkPreconditions(u:Member,p:PredictedEvent):Boolean = {
     if (alternative <0 || alternative >= p.nAlternatives) {
       throw new IllegalArgumentException("invalid alternative");
     } else if (u.balance < sum) {
       throw new IllegalArgumentException("too small balance");
     } else if (p.closed) {
       throw new IllegalArgumentException("prediction is closed");
     } else if (p.stopBidding.before(now)) {
       throw new IllegalArgumentException("bidding is stopped");
     } else true
   }

   for(u <- members.filter(_.id === userId).result.head;
       p <- predictedEvents.filter(_.id === eventId).result.head;
       checked = checkPreconditions(u,p);
       b <- (members.filter(_.id===userId).update(u.copy(balance = (u.balance - sum)))
            andThen(
             (predictedEvents.filter(_.id===eventId) update p.copy(actualSum = p.actualSum+sum))
             andThen
              insertOrUpdateBids()
           )) if checked
       ) yield b
  }
          

  def markPredictionResult(eventId: Long,
                           alternative: Int): DBIO[Unit] =
  {
   def eventQuery = predictedEvents.filter(_.id===eventId)
   
   def checkClosed(p:PredictedEvent):Boolean =
   {
     if (p.closed) {
       throw new IllegalArgumentException("p is already closed");
     }
     true
   }

   // 
   def checkPreconditions(p:PredictedEvent, bids:Seq[Bid]):Boolean =
   {
     !bids.isEmpty && bids.length != 1 && p.actualSum >= p.minSum
   }

   def distribute(bids:Seq[Bid], p:PredictedEvent): DBIO[Boolean] =
   {
     val byAlternatives = bids.groupBy(_.alternative);
     byAlternatives.get(alternative) match {
        case None => returnBids(bids,p) 
        case Some(rightBids) => 
                   val rightSum = rightBids.map(_.sum).sum;
                   val allSum = bids.map(_.sum).sum;
                   val up = allSum*(1.0-organizerComission);
                   val down = rightSum;
                   val distributions = (
                     rightBids map { bid =>
                        val gain = (bid.sum * up / down).setScale(2,BigDecimal.RoundingMode.HALF_EVEN);
                        val mq = members.filter(_.id===bid.memberId) map (_.balance)
                        for(oldBalance <- mq.result.head;
                            up <- mq update (oldBalance + gain)) yield up
                     }
                   )
                   DBIO.seq(distributions: _*) map (_ => true)
     }
     
   }

   val result = for(e <- eventQuery.result.head if (checkClosed(e));
       bs <- bids.filter(_.eventId === eventId).result;
       r <- if(checkPreconditions(e,bs)) distribute(bs,e) else returnBids(bs,e);
       u <- eventQuery.map(x=>(x.closed,x.failed,x.result)).update(true,Some(r),Some(alternative))
      ) yield u

   result map ( _ => ())
  }

  private def returnBids(bids: Seq[Bid], p:PredictedEvent): DBIO[Boolean] = 
  { 
     val returns = for(bid <- bids) yield {
       val q = members.filter(_.id === bid.memberId) map (_.balance)
       for( oldBalance <- q.result.head;
            up <- q update (oldBalance + bid.sum) ) yield up
     } 
     DBIO.seq(returns: _*) map ( _ => false)
  }

  def organizerComission = 0.01;

}

// vim: set ts=4 sw=4 et:
