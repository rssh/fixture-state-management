package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import org.scalatest.managedfixture._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1SFlatSpecTest extends managedfixture.FlatSpec[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  execution autonomic
  
  import Base1FixtureStateInfo.States._;

    behavior of "system 1" 

    start state(TWO) change(nothing)
    it should "work in state 2" in { x =>
      assert(x==2);
    }

    start state(ONE) finish state(TWO)
    it should "work in state 1" in { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
    }

    behavior of "system 2"

    start state(TWO) finish state(undefined)
    it should "work in state 2" in { x =>
       assert(x==2);
    }
  

}



// vim: set ts=4 sw=4 et:
