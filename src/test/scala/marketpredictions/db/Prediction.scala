package marketpredictions.db

import java.sql.Timestamp;

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;

import MPSchema._;

case class Prediction(val id: Long,
                      val description: String,
                      val nAlternatives: Int,
                      val passTime: Timestamp,
                      val startBidding: Timestamp,
                      val stopBidding: Timestamp,
                      val actualSum: BigDecimal,
                      val minSum: BigDecimal,
                      val closed: Boolean,
                      val failed: Option[Boolean],
                      val result:  Option[Int],
                      val authorId: Long
                    ) extends KeyedEntity[Long]
{

  def this() = this(id=0L,
                    description="",
                    nAlternatives=0,
                    passTime = new Timestamp(0L),
                    startBidding = new Timestamp(0L),
                    stopBidding = new Timestamp(0L),
                    actualSum = BigDecimal(0L),
                    minSum = BigDecimal(0L),
                    closed=false,
                    failed=Some(false),
                    result=Some(0),
                    authorId=0L);
 
  lazy val author: ManyToOne[Member] = authority.right(this);
  
  lazy val bids = from(MPSchema.bids)(b => where(b.predictionId===id)
                                                               select(b));

}
                                 


object Prediction
{


  def schemaInit: Unit =
   inTransaction {
   }

   def schemaClear:Unit = inTransaction {
   }

}


// vim: set ts=4 sw=4 et:
