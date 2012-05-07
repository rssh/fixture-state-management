package ua.gradsoft.scalatest.state.internal

import org.scalatest._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1SPropSpecTest extends managedfixture.PropSpec[Base1FixtureStateInfo.type]
{

 val fixtureStateTypes = Base1FixtureStateInfo;
 val fixtureAccess = Base1FixtureAccess;
 import Base1FixtureStateInfo.States._;

 execution autonomic
 
 start state(TWO) change(nothing)
 property("should work in state 2 [PropSpec]") { x =>
      assert(x==2);
 }

 start state(ONE) finish state(TWO)
 property("should work in state 1 [PropSpec]") { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
 }


 start state(TWO) finish state(undefined)
 property("U1: should work in state 2 [PropSpec]") { x =>
       assert(x==2);
       fixtureAccess.set(THREE);
 }
  
    
 start state(ONE) finish state(undefined)
 property("U2: should work in state 2 2 [PropSpec]") { x =>
          assert(x==1);
          fixtureAccess.set(THREE);
 }

 start state(any) change(nothing)
 property("wihtout fixture [PropSpec]") { () =>
       assert(true);
 }


}



// vim: set ts=4 sw=4 et:
