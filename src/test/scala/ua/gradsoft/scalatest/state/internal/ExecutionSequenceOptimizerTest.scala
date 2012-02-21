package ua.gradsoft.scalatest.state.internal

import org.scalatest._;
import ua.gradsoft.testing._;

import Base1FixtureStateInfo.States._;


class ExecutionSequenceOptimizerTest extends FunSuite
                                       with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  type FST = Base1FixtureStateInfo.type;

  test("optimize 3 simple cases")  {
    var m = Map[String, TestFixtureStateUsageDescription[FST]](
                  "p2" ->  (start state(TWO) change(nothing)).value ,
                  "p1" ->  (start state(ONE) finish state(TWO)).value,
                  "p3" ->  (start state(TWO) finish state(THREE)).value
    );
    val seq = ExecutionSequenceOptimizer.optimizeOrder(m);
    assert(seq.size == 3);
    assert(seq(0)(0)=="p1");
    assert(seq(1)(0)=="p2");
    assert(seq(2)(0)=="p3");
  }

}

// vim: set ts=4 sw=4 et:
