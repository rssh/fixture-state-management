package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1S4Test extends managedfixture.FunSuite[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  import Base1FixtureStateInfo.States._;

  execution autonomic
  
  start state(TWO) change(nothing)
  test("withDSL [4]: start state(TWO) change nothing") { x =>
    assert(x==2);
    assert(Base1S4TestMarkObject.x == "afterONE");
    Base1S4TestMarkObject.x = "afterTWO";
  }

  start state(ONE) finish state(TWO)
  test("withDSL [4]: start state(ONE) finish state(TWO)") { x =>
    assert(x==1);
    Base1S4TestMarkObject.x = "afterONE";
    fixtureAccess.set(TWO);    
  }

  start state(TWO) finish state(undefined)
  test("withDSL [4]: start state(TWO) finish state(undefined)") { x =>
    assert(x==2);
    // must be called after test with same state which change nothing.
    assert(Base1S4TestMarkObject.x == "afterTWO");
  }


}


object Base1S4TestMarkObject
{
  var x: String = "ini";
}

// vim: set ts=4 sw=4 et:
