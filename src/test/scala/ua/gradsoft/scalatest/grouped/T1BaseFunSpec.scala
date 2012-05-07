package ua.gradsoft.scalatest.grouped

import org.scalatest._
import ua.gradsoft.scalatest.state.internal._

class T1BaseFunSpec extends managedfixture.FunSpec[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  import Base1FixtureStateInfo.States._;

  describe("grouped: [T1FunSpec]" ) {

    start state(TWO) change(nothing)
    it("should work in state 2") { x =>
      assert(x==2);
      FunSpecGroup.t1s2done = true;
    }

    start state(ONE) finish state(TWO)
    it("should work in state 1") { x =>
      assert(x==1);
      fixtureAccess.set(TWO);
      FunSpecGroup.t1s1done = true;
    }

 }


}


// vim: set ts=4 sw=4 et:
