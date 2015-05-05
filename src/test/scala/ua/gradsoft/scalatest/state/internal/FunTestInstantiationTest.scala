package ua.gradsoft.scalatest.state.internal


import scala.concurrent._
import org.scalatest._
import ua.gradsoft.managedfixture._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicInteger

class MyFunGroupSuite extends managedfixture.GroupSuite[AtomicInteger,Int]
{

   val fixtureAccessBoxFactory = new FixtureAccessBoxFactory[AtomicInteger] {
          def box() = Future successful _box
          def close() = Future successful (())
          def nBoxes: Option[Int] = Some(1)
   }

   val _box = new FixtureAccessBox[AtomicInteger] {

       def apply[A](f: AtomicInteger=>A ): Future[(A, this.type)] = 
       {
          val lp: Future[(A,this.type)] = last map { _ => (f(v), this ) }
          last = lp map (_._2)
          lp
       }

       def close(): scala.concurrent.Future[Unit] = Future successful (())

       private[this] var last: Future[this.type] = Future successful this
       private[this] var v:AtomicInteger = new AtomicInteger(0)
   }

}

class MyFunTest(g: managedfixture.GroupSuite[AtomicInteger,Int],
                f: Option[AtomicInteger],
                testToRun:Option[String]) 
                   extends managedfixture.FunSuite[AtomicInteger,Int](g,f,testToRun)
{

  start state(2) change nothing
  test("TEST-2") { x =>
     assert(x==2)
  }

  start state(3) change nothing
  test("TEST-3") { x =>
     assert(x==3)
  }
  
  start state(any) finish state(2)
  test("TEST:any->2") { x =>
     x.set(2)
  }

  start state(any) finish state(3)
  test("TEST:any->3") { x =>
     x.set(3)
  }

}

class FunTestInstantiationTest extends FunSuite
{

  test("fixture FunTest must be able to instantiate yourself") {
    val g = new MyFunGroupSuite
    System.err.println("v1: g.registeredTests="+g.registeredTests);
    val t0 = new MyFunTest(g,None,None)
    val t1_2 = t0.createCopy(g,Some(new AtomicInteger(2)),Some("TEST-2"))
    assert(t1_2.isInstanceOf[MyFunTest])
    val t1_3 = t0.createCopy(g,Some(new AtomicInteger(3)),Some("TEST-3"))
    System.err.println("v2: g.registeredTests="+g.registeredTests);
    //t1_2.run
  }

  test("ff") {
    pending
  }


}

// vim: set ts=4 sw=4 et:
