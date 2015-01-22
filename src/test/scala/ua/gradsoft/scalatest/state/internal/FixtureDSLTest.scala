package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._


class FixtureDSLTest extends FunSuite
                           with FixtureStateDSL[Int, Int]
{

  def fixtureUsage  = new FixtureStateVerb;

  test("fixtureUsageAnyState start state(any)") {
    val x = fixtureUsage start state(any)
    assert(x.value.precondition == AnyState);
  }

  test("fixtureUsage start state (s)") {
    val x = fixtureUsage start state (2) ;
    assert(x.value.precondition == States(Set(2)));
  }


  test("fixtureUsage start state(any) finish state(undefined)") {
    val x = fixtureUsage start state(any) finish state(undefined)
    assert(x.value.startStateChange == UndefinedState);
  }

  test("fixtureUsage start state(s) finish state(s1)") {
    val x = fixtureUsage start state(2) finish state(1)
    assert(x.value.startStateChange == NewState(1))
  }

  test("fixtureUsage start state(s) change(nothing) ") {
    val x = fixtureUsage start state(2) change ( nothing )
    assert(x.value.startStateChange == SameState);
  }

  test("start state(s) change(nothing) ") {
    val x = start state(2) change ( nothing )
    assert(x.value.startStateChange == SameState);
  }

  test("start state(s) change(nothing) execution(parallel)") {
    val x = start state(2) change ( nothing ) execution(parallel)
    assert(x.value.startStateChange == SameState);
    assert(x.value.canRunParallel == true);
  }

  test("start state(s) change(nothing) execution(sequential)") {
    val x = start state(2) change ( nothing ) execution(sequential)
    assert(x.value.startStateChange == SameState);
    assert(x.value.canRunParallel == false);
  }

}

// vim: set ts=4 sw=4 et:
