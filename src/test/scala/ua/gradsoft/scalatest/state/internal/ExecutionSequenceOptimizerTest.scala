package ua.gradsoft.scalatest.state.internal

import org.scalatest._;
import ua.gradsoft.managedfixture._;


class ExecutionSequenceOptimizerTest extends FunSuite
                                       with FixtureStateDSL[Int,Int]
{


  test("optimize 3 simple cases")  {
    var m = Map[String, FixtureStateUsageDescription[Int]](
                  "p2" ->  (start state(2) change(nothing)).value ,
                  "p1" ->  (start state(1) finish state(1)).value,
                  "p3" ->  (start state(2) finish state(3)).value
    )
    pending
/*
    val seq = ExecutionSequenceOptimizer.optimizeOrder(m);
    assert(seq.size == 3);
    assert(seq(0)(0)=="p1");
    assert(seq(1)(0)=="p2");
    assert(seq(2)(0)=="p3");
*/
  }

}

// vim: set ts=4 sw=4 et:
