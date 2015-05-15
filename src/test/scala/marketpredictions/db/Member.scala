package marketpredictions.db

import java.sql.Timestamp;

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


case class Member(val id: Long,
                  val name: String,
                  val balance: BigDecimal
                 ) 
                                 

class Members(tag:Tag) extends Table[Member](tag,"members")
{
  def id = column[Long]("id",O.PrimaryKey)
  def name = column[String]("name")
  def balance = column[BigDecimal]("balance")

  def * = (id, name, balance) <> (Member.tupled, Member.unapply)

}


// vim: set ts=4 sw=4 et:
