package ua.gradsoft.scalatest.state.internal

import org.scalatest._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1SFunSpecTest extends managedfixture.FunSpec[Base1FixtureStateInfo.type]
{


  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  import Base1FixtureStateInfo.States._;

  execution autonomic
  
  describe("system 1 [FunSpec]" ) {

    start state(TWO) change(nothing)
    it("should work in state 2") { x =>
      assert(x==2);
    }

    start state(ONE) finish state(TWO)
    it("should work in state 1") { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
    }

 }

 describe("system 2 [FunSpec]") {

    start state(TWO) finish state(undefined)
    it("should work in state 2") { x =>
       assert(x==2);
    }
  
 }
 

}



// vim: set ts=4 sw=4 et:
