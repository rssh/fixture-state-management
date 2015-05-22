package marketpredictions.db

import java.sql.Timestamp;

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import MPSchema._;


case class PredictedEvent(val id: Option[Long],
                      val idname: String,
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
                    )
                                 
class PredictedEvents(tag:Tag) extends Table[PredictedEvent](tag,"predicted_events")
{
  def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
  def idname = column[String]("idname")
  def description = column[String]("description")
  def nAlternatives = column[Int]("n_alternatives")
  def passTime = column[Timestamp]("pass_time")
  def startBidding = column[Timestamp]("start_bidding")
  def stopBidding = column[Timestamp]("stop_bidding")
  def actualSum = column[BigDecimal]("actual_sum")
  def minSum = column[BigDecimal]("min_sum")
  def closed = column[Boolean]("closed")
  def failed = column[Option[Boolean]]("failed")
  def result = column[Option[Int]]("result")
  def authorId = column[Long]("author_id")

  def * = (id.? , idname, description, nAlternatives, passTime, startBidding, stopBidding,
           actualSum, minSum, closed, failed, result, authorId) <> (PredictedEvent.tupled,
                                                                    PredictedEvent.unapply)

  def idnameIndex = index("predicted_events_idname_index",idname, unique=true)

  def author = foreignKey("predicted_events_author_fk",authorId,members)(_.id)

}



// vim: set ts=4 sw=4 et:
