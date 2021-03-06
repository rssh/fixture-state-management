package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._


class FixtureDSLTest extends FunSuite
                           with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;

  def fixtureUsage  = new FixtureStateVerb;

  test("fixtureUsageAnyState start state(any)") {
    val x = fixtureUsage start state(any)
    assert(x.value.precondition.allowedStartStates.size == fixtureStateTypes.States.values.size);
  }

  test("fixtureUsage start state (s)") {
    val x = fixtureUsage start state (Base1FixtureStateInfo.States.TWO) ;
    assert(x.value.precondition.allowedStartStates.size == 1);
  }

  test("fixtureUsage start state (s) aspects(a)") {
    val x = fixtureUsage start state (Base1FixtureStateInfo.States.TWO) aspects (
                                               Base1FixtureStateInfo.stateAspects(0));
    assert(x.value.precondition.neededStateAspects.size == 1);
  }

  test("fixtureUsage start state(any) finish state(undefined)") {
    val x = fixtureUsage start state(any) finish state(undefined)
    assert(x.value.startStateChange == UndefinedState);
  }

  test("fixtureUsage start state(s) finish state(s1)") {
    val x = fixtureUsage start state(Base1FixtureStateInfo.States.TWO
                                    ) finish state(
                                        Base1FixtureStateInfo.States.ONE)
    assert(x.value.startStateChange == NewState[Base1FixtureStateInfo.type](Base1FixtureStateInfo.States.ONE));
  }

  test("fixtureUsage start state(s) aspects(a) finish state(s1)") {
    val x = fixtureUsage start state(Base1FixtureStateInfo.States.TWO
                                    ) aspects (
                                       Base1FixtureStateInfo.stateAspects(0)
                                    ) finish state(
                                        Base1FixtureStateInfo.States.ONE)
    assert(x.value.startStateChange == NewState[Base1FixtureStateInfo.type](Base1FixtureStateInfo.States.ONE));
    assert(x.value.precondition.neededStateAspects.size == 1);
  }

  test("fixtureUsage start state(s) change(nothing) ") {
    val x = fixtureUsage start state(Base1FixtureStateInfo.States.TWO
                                    ) change ( nothing )
    assert(x.value.startStateChange == SameState);
  }

  test("start state(s) change(nothing) ") {
    val x = start state(Base1FixtureStateInfo.States.TWO
                                    ) change ( nothing )
    assert(x.value.startStateChange == SameState);
  }

  test("start state(s) change(nothing) execution(parallel)") {
    val x = start state(Base1FixtureStateInfo.States.TWO
                                    ) change ( nothing ) execution(parallel)
    assert(x.value.startStateChange == SameState);
    assert(x.value.canRunParallel == true);
  }

  test("start state(s) change(nothing) execution(sequential)") {
    val x = start state(Base1FixtureStateInfo.States.TWO
                                    ) change ( nothing ) execution(sequential)
    assert(x.value.startStateChange == SameState);
    assert(x.value.canRunParallel == false);
  }

}

// vim: set ts=4 sw=4 et:
