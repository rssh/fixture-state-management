package marketpredictions.db

import java.sql.Timestamp;

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class Bid(val memberId: Long,
                val eventId: Long,
                val alternative: Int,
                val sum: BigDecimal,
                val when: Timestamp
              ) 

class Bids(tag: Tag) extends Table[Bid](tag,"bids")
{
   def memberId = column[Long]("member_id")
   def eventId = column[Long]("event_id")
   def alternative = column[Int]("alternative")
   def sum = column[BigDecimal]("sum")
   def when = column[Timestamp]("when")

   def pk = primaryKey("bids_pk",(memberId,eventId))

   def * = (memberId, eventId, alternative, sum, when) <> (Bid.tupled, Bid.unapply)

//   def member = foreign_key("bids_member_fk",member_id,
   

}



// vim: set ts=4 sw=4 et:
