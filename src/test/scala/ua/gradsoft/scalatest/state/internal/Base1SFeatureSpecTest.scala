package ua.gradsoft.scalatest.state.internal

import org.scalatest._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1SFeatureSpecTest extends managedfixture.FeatureSpec[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  import Base1FixtureStateInfo.States._;

  execution autonomic
  
  feature("f1 [FeatureSpec]" ) {

    start state(TWO) change(nothing)
    scenario("should work in state 2 [FeatureSpec]") { x =>
      assert(x==2);
    }

    start state(ONE) finish state(TWO)
    scenario("should work in state 1 [FeatureSpec]") { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
    }

 }

 feature("f2 2 [FeatureSpec]") {

    start state(TWO) finish state(undefined)
    scenario("should work in state 2 [FeatureSpec]") { x =>
       assert(x==2);
    }
  
    start state(TWO) finish state(undefined)
    ignore("ignored in state 2 [FeatureSpec]") { x =>
       assert(x==2);
    }

 }
 

}



// vim: set ts=4 sw=4 et:
