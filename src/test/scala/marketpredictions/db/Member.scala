package marketpredictions.db

import java.sql.Timestamp;

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;


case class Member(val id: Long,
                  val nick: String,
                  val balance: BigDecimal
                 ) extends KeyedEntity[Long]
{

  def this() = this(id=0L,
                    nick="",
                    balance = BigDecimal(0L)
                    );
  
}
                                 


object Member
{

  import MPSchema._;

  def schemaInit: Unit =
   inTransaction {
   }

   def schemaClear:Unit = inTransaction {
   }

}


// vim: set ts=4 sw=4 et:
