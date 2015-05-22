package marketpredictions.engine

import marketpredictions.db._
import marketpredictions.db.MPSchema._

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

trait PaymentOperations
{

 this: Api => 


 def payIn(userId: Long, sum: BigDecimal): DBIO[Unit] =
  {
    val q = members.filter(_.id===userId).map(_.balance)
    for(oldBalance <- q.result.head;
        up <- q.update(oldBalance + sum);
        v = () ) yield v
  }.transactionally


 def payOut(userId: Long, sum: BigDecimal): DBIO[Unit] =
 {
    val q = members.filter(_.id===userId).map(_.balance)

    def checkFunds(x:BigDecimal):Boolean =
      if (x < sum) { 
         throw new IllegalArgumentException("Insifficient balance")
      } else true

    for(oldBalance <- q.result.head if checkFunds(oldBalance);
        up <- q.update(oldBalance - sum);
        v=() ) yield v
  }
        
}

// vim: set ts=4 sw=4 et:
