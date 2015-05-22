package marketpredictions.engine

import marketpredictions.db._
import marketpredictions.db.MPSchema._

import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


trait UserOperations
{

 this: Api => 


 def createUser(name: String): DBIOAction[Long,NoStream,Effect.Write] =
    (members returning (members map (_.id))) += Member(None, name, BigDecimal(0L))

 def findUser(name: String): DBIOAction[Option[Member],NoStream,Effect.Read] =
  members.filter(_.name === name).result.headOption

 def dropUser(id: Long): DBIOAction[Boolean,NoStream,Effect.Write] =
  members.filter(_.id === id).delete map (_ != 0)

}

// vim: set ts=4 sw=4 et:
