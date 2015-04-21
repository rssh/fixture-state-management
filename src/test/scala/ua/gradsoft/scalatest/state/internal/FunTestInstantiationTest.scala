package ua.gradsoft.scalatest.state.internal


import scala.concurrent._
import org.scalatest._
import ua.gradsoft.managedfixture._
import scala.concurrent.ExecutionContext.Implicits.global

class MyGroupSuite extends managedfixture.GroupSuite[Int,Int]
{
   def fixtureAccessBoxFactory = new FixtureAccessBoxFactory[Int,Int] {
          def box() = Future successful _box
          def close() = Future successful (())
          def nBoxes: Option[Int] = Some(1)
   }

   val _box = new FixtureAccessBox[Int,Int] {

       def apply[A](op: FixtureAccessOperation[A,Int,Int]): Future[(A, this.type)] = 
       {
          val lp: Future[(A,this.type)] = last map { _ => (op.f(v), this ) }
          last = lp map (_._2)
          lp
       }

       def close(): scala.concurrent.Future[Unit] = Future successful (())

       private[this] var last: Future[this.type] = Future successful this
       private[this] var v:Int = 0;
   }
}

class MyFunTest(g: managedfixture.GroupSuite[Int,Int],f:Option[Int],testToRun:Option[String]) 
               extends managedfixture.FunSuite[Int,Int](g,f,testToRun)
{

  start state(2) change nothing
  test("TEST-2") { x =>
     assert(x==2)
  }

  start state(3) change nothing
  test("TEST-3") { x =>
     assert(x==3)
  }


}

class FunTestInstantiationTest extends FunSuite
{

  test("fixture FunTest must be able to instantiate yourself") {
    val g = new MyGroupSuite
    val t0 = new MyFunTest(g,None,None)
    val t1_2 = t0.createCopy(g,Some(2),Some("TEST-2"))
    assert(t1_2.isInstanceOf[MyFunTest])
    val t1_3 = t0.createCopy(g,Some(3),Some("TEST-3"))
    //t1_2.run
  }

  test("ff") {
    pending
  }


}

// vim: set ts=4 sw=4 et:
