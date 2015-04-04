package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._

class MyGroupSuite extends managedfixture.GroupSuite

class MyFunTest(g: managedfixture.GroupSuite,f:Option[Int],testToRun:Option[String]) 
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
