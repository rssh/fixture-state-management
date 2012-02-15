package ua.gradsoft.scalates.state.internal

import org.scalatest._
import ua.gradsoft.scalatest.state._



class Base1SimpleTest extends FunSuite
{

  def future: Base1FixtureStateInfo.FixtureType = 1;

  test("test nothing to start") {
    assert(true);
  }

}



// vim: set ts=4 sw=4 et:
