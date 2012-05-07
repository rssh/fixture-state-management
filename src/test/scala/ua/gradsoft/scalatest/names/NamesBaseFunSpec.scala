package ua.gradsoft.scalatest.names

import org.scalatest._
import ua.gradsoft.scalatest.state.internal._

class NamesBaseFunSpec extends managedfixture.FunSpec[Base1FixtureStateInfo.type]
{

  execution autonomic

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  import Base1FixtureStateInfo.States._;

  describe("names: [BaseFunSpec] with extraws" ) {

    start state(TWO) change(nothing)
    it("should work in state 2 ") { x =>
      assert(x==2);
    }

 }


}


// vim: set ts=4 sw=4 et:
