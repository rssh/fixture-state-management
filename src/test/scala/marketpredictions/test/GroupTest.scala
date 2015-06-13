package marketpredictions.test


import scala.concurrent._
import org.scalatest._
import ua.gradsoft.managedfixture._
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

class MPGroupSuite extends managedfixture.GroupSuite[Database,DBState]
{

   val fixtureAccessBoxFactory = new OneInstanceFixtureAccessBoxFactory[Database](
                                      Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver="org.h2.Driver"))

}

// vim: set ts=4 sw=4 et:
