package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._


class FixtureDSLTest extends FunSuite
                           with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;

  def fixtureUsage  = new FixtureStateVerb;

  test("fixtureUsageAnyState start state any") {
    val x = fixtureUsage start state(any)
    assert(x.value.precondition.allowedStartStates.size == fixtureStateTypes.States.values.size);
  }

  test("fixtureUsage start state (s)") {
    val x = fixtureUsage start state (Base1FixtureStateInfo.States.TWO) ;
    assert(x.value.precondition.allowedStartStates.size == 1);
  }

  /*
    fixtureUsage state(IntialDatabase) change nothing
  */

}

// vim: set ts=4 sw=4 et:
