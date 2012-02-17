package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._


class FixtureDSLTest extends FunSuite
                           with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;

  def fixtureUsage  = new FixtureStateVerb;

  test("fixtureUsageAnyState must be state") {
    var x = fixtureUsage any state
    assert(x.value.precondition.allowedStartStates.size == fixtureStateTypes.States.values.size);
  }

  test("fixtureUsage state (s)") {
    var x = fixtureUsage state (Base1FixturestateInfo.States.TWO)
    assert(x.value.precondition.allowedStartStates.size == 1);
  }

  /*
    fixtureUsage state(IntialDatabase) change nothing
  */

}

// vim: set ts=4 sw=4 et:
