package marketpredictions.engine

import marketpredictions.db._
import marketpredictions.db.MPSchema._

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;


trait PaymentOperations
{

 this: Api => 


 def payIn(userId: Long, sum: BigDecimal): Unit =
  inTransaction {
    update(members)(u=>where(u.id===userId)
                       set(u.balance := u.balance+sum));
  }

 def payOut(userId: Long, sum: BigDecimal): Unit =
  inTransaction {
    members.lookup(userId) match {
       case Some(u) => if (u.balance < sum) {
                          throw new IllegalArgumentException("Insifficient balance");
                       }
                       update(members)(u=>where(u.id===userId)
                                      set(u.balance := u.balance - sum))
       case None =>
                throw new IllegalArgumentException("Invalid userId");
    }
  }

}

// vim: set ts=4 sw=4 et:
