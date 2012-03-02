package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._
import ua.gradsoft.scalatest._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1SFreeSpecTest extends managedfixture.FreeSpec[Base1FixtureStateInfo.type]
{

 val fixtureStateTypes = Base1FixtureStateInfo;
 val fixtureAccess = Base1FixtureAccess;

 import Base1FixtureStateInfo.States._;

 "f1 [FreeSpec]" - {

    start state(TWO) change(nothing)
    "should work in state 2 [FreeSpec]" in { x =>
      assert(x==2);
    }

    start state(ONE) finish state(TWO)
    "should work in state 1 [FreeSpec]" in { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
    }

 }

 "f2 [FreeSpec]" - {

    start state(TWO) finish state(undefined)
    "should work in state 2 [FreeSpec]" in { x =>
       assert(x==2);
    }
  
    
    start state(ONE) finish state(undefined)
    "f2 2 [FreeSpec]" - {
       "should work in state 2 2 [FreeSpec]" in { x =>
          assert(x==1);
       }
    }

    start state(TWO) finish state(undefined)
    "ignored in state 2 [FeatureSpec]" ignore { x =>
       assert(x==2);
    }

 }

}



// vim: set ts=4 sw=4 et:
