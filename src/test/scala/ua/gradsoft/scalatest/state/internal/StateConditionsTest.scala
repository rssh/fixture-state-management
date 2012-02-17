package ua.gradsoft.scalates.state.internal

import org.scalatest._
import ua.gradsoft.testing._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class StateConditionsTest extends FunSuite
{

  import Base1FixtureStateInfo.States._;


  test("check that condition is choosed from set") { 
    def precondition = new SetOfStatesAndAspects[Base1FixtureStateInfo.type](Base1FixtureStateInfo,
                                                                        Set(TWO),
                                                                        Set());
    val state = precondition.stateToLoad;
    assert(state == TWO);
  }



}



// vim: set ts=4 sw=4 et:
