package marketpredictions.test

import scala.language.postfixOps

import org.scalatest._
import org.scalatest.Assertions._
import slick.driver.H2Driver.api._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



class BidFeatureSpecTest(g: managedfixture.GroupSuite[Database,DBState],
                         f: Option[Database],
                         testToRun: Option[String])
                          extends managedfixture.FunSuite[Database,DBState](g,f,testToRun)
{


   //("bidding") 
      
       start state(S3_MORE_PREDICTIONS) 
       test("can't bid more money than user have") {
          db => 
           System.err.println("db="+db)
           val api = TestApi(db, CalendarUtil.timestamp(2012,1,2,0,0) )
           val actions = for{ 
               bob <- api.findUser("bob") map (_.get);
               event <- api.findEvent("ab") map (_.get);
               r <- api.bid(bob.id.get, event.id.get, 1, bob.balance+1.0)
           } yield r
           intercept[Exception] {
               val f = db.run(actions) 
               f onComplete (x => System.err.println("!!!can't bid.. result: "+x))
               Await.result( f, 1 minute)
           }
       }

       start state(S3_MORE_PREDICTIONS) 
       test("when only one bid, money is returned except comission") {
          db => 
           val api1 = TestApi(db, CalendarUtil.timestamp(2012,1,2,0,0) )
           val actions = for{
                            bob1 <- api1.findUser("bob") map (_.get);
                            event <- api1.findEvent("ab") map (_.get);
                            r1 <- api1.bid(bob1.id.get, event.id.get, 1, bob1.balance);
                            bob2 <- api1.findUser("bob") map (_.get);
                            q1 = bob2.balance < BigDecimal(0.0001);
                            api2 = api1.copy(now = CalendarUtil.addDays(event.passTime,1));
                            r2 <- api2.markPredictionResult(event.id.get,0);
                            // now bob must have balance greater than zeor
                            bob3 <- api2.findUser("bob") map (_.get);
                            q2 = bob3.balance > BigDecimal(0.0001)
                         } yield ((q1, q2))
            Await.result(db.run(actions), 1 minute) match {
               case ((q1,q2)) =>
                               assert(q1,"first bid must be successful")
                               assert(q2,"at the end money must be returned")
            }
       }



}

// vim: set ts=4 sw=4 et:
