package marketpredictions.test

import java.sql._;

import org.scalatest._
import ua.gradsoft.managedfixture._

import slick.driver.H2Driver.api._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import marketpredictions.db._
import marketpredictions.engine._

import scala.language.postfixOps

sealed trait DBState

case object EMPTY extends DBState
case object S1_USERS_WITHOUT_MONEY extends DBState
case object S2_USERS_WITH_MONEY extends DBState
case object S3_MORE_PREDICTIONS extends DBState

class LoadStatesTest(g: managedfixture.GroupSuite[Database,DBState],
                     f: Option[Database],
                     testToRun:Option[String])
                   extends managedfixture.FunSuite[Database,DBState](g,f,testToRun)
{

  System.err.println("LoadStateTestConstructor, g="+g+", testToRun:"+testToRun)

  start state(any) finish state(EMPTY)
  test("load empty state") { db =>
       System.err.println("load empty state:start");
       Await.ready(db.run(recreateDB(db)), 1 minute)
       System.err.println("load empty state:end");
  }

  start state(EMPTY) finish state(S1_USERS_WITHOUT_MONEY)
  test("create users") { db =>
      System.err.println("load s1 state:start");
      val testApi = TestApi(db, CalendarUtil.timestamp(2012,1,1,0,0) )
      val io = for{ 
           aliceId <- testApi.createUser("alice") ;
           bobId <- testApi.createUser("bob") ;
           alexId <- testApi.createUser("alex")     
         } yield (aliceId, bobId, alexId)
      val f = db.run(io)
      val r = Await.ready(f, 1 minute)
      System.err.println("load s1 state:end");
  }

  start state(S1_USERS_WITHOUT_MONEY) finish state(S2_USERS_WITH_MONEY)
  test("load s2 state= give money to users") { db =>
    System.err.println("load s2 state:start");
    val testApi = TestApi(db, CalendarUtil.timestamp(2012,2,1,0,0));
    val io = for{
      alice <- testApi.findUser("alice") map (_.get);
      bob <- testApi.findUser("bob") map (_.get);
      alex <- testApi.findUser("alex") map (_.get);
      p1 <- testApi.payIn(alice.id.get,BigDecimal(100L));
      p2 <- testApi.payIn(bob.id.get,BigDecimal(100L));
      p3 <- testApi.payIn(alex.id.get,BigDecimal(100L))
    } yield (())
    val f = db.run(io)
    val r = Await.ready(f, 1 minute)
    System.err.println("load s2 state:end");
  }

  start state(S2_USERS_WITH_MONEY) finish state(S3_MORE_PREDICTIONS)
  test("load s3 state") { db =>
    System.err.println("load s3 state:start");
    val testApi = TestApi(db, CalendarUtil.timestamp(2012,2,1,0,0));
    val io = for{
       alice <- testApi.findUser("alice") map (_.get);
       alex <- testApi.findUser("alex") map (_.get);
       // alice open auction on 100
       alicePay <- testApi.payIn(alice.id.get,BigDecimal(100L));
       ce1 <-  testApi.createEvent( alice.id.get , "ab",
                           "A and B are sitting on the tube. Who will left fisrt. [0-A,1-B]",
                           2,
                           CalendarUtil.timestamp(2012,3,1,0,0),
                           20);
      // bob on 50
        bob <- testApi.findUser("bob") map (_.get);
        ce2 <- testApi.createEvent( bob.id.get , "p2012",
                           "who will win 2012 parlament elections  [0-opposition,1-power]",
                            2,
                           CalendarUtil.timestamp(2012,11,1,0,0),
                          0)
     } yield ce2
     val f = db.run(io)
     val r = Await.ready(f, 1 minute)
     System.err.println("load s3 state:end");
  }

  private def recreateDB(db: Database): DBIO[Unit] = {
    DBIO.seq(
      MPSchema.preClear,
      MPSchema.drop,
      MPSchema.create,
      MPSchema.postInit
      //createTestStatesTableIfNeeded
    )
  }


}

// vim: set ts=4 sw=4 et:
