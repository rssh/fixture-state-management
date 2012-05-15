package marketpredictions.db

import java.sql.Timestamp;

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;

import MPSchema._;

case class Bid(val memberId: Long,
               val eventId: Long,
               val alternative: Int,
               val sum: BigDecimal,
               val when: Timestamp
              ) extends KeyedEntity[CompositeKey2[Long,Long]]
{

  def this() = this(memberId=0L,
                    eventId=0L,
                    alternative=0,
                    sum = BigDecimal(0L),
                    when = new Timestamp(0L)
                    );
 
  def id = CompositeKey2(memberId, eventId);

  //lazy val members: ManyToMany[Member] = bids.right(this);
  
}
                                 


object Bid
{


  def schemaInit: Unit =
   inTransaction {
   }

   def schemaClear:Unit = inTransaction {
   }

}


// vim: set ts=4 sw=4 et:
