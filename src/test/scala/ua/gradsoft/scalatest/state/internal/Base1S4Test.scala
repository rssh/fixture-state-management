package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.collection.immutable.ListSet;
import scala.util._;


class Base1S4Test extends fixture.FunSuite
                             with FixtureStateDSL[Base1FixtureStateInfo.type]
{


  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  type FST = Base1FixtureStateInfo.type;
  type FixtureParam = FST#FixtureType;

  lazy val stateManager = new FixtureStateManager[FST](fixtureAccess);

  lazy val dummyStateData = TestFixtureStateUsageDescription[FST](fixtureStateTypes).withAnyState;

  var currentFixtureData = TestFixtureStateUsageDescription[FST](fixtureStateTypes);

  lazy val testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[FST]] = LinkedHashMap();

  def fixtureUsage(x: DSLExpression) =
  {  currentFixtureData = x.value; }

  def withFixture(test: OneArgTest)
  {
    val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    stateManager.doWith(x,test);
  }

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
      testStateUsageDescriptions(testName) = currentFixtureData;
      super.test(testName, testTags: _* )(testFun);
  }

  import Base1FixtureStateInfo.States._;

  // test, to check that we run all those tst sequentially in some order.

  override def testNames: Set[String] = 
   {
    val l = ExecutionSequenceOptimizer.optimizeOrder(testStateUsageDescriptions).flatMap(identity)
    throw new Exception("AAA");
    System.err.println("l is:"+l);
    ListSet(l:_*);
   }

  fixtureUsage(start state(TWO) change(nothing))
  ignore("withDSL [4]: start state(TWO) change nothing") { x =>
    assert(x==2);
    assert(Base1S4TestMarkObject.x == "afterONE");
    Base1S4TestMarkObject.x = "afterTWO";
  }

  fixtureUsage(start state(ONE) finish state(TWO))
  ignore("withDSL [4]: start state(ONE) finish state(TWO)") { x =>
    assert(x==1);
    Base1S4TestMarkObject.x = "afterONE";
    fixtureAccess.set(TWO);    
  }

  fixtureUsage(start state(TWO) finish state(undefined))
  ignore("withDSL [4]: start state(TWO) finish state(undefined)") { x =>
    assert(x==2);
    // must be called after test with same state which change nothing.
    assert(Base1S4TestMarkObject.x == "afterTWO");
  }


}


object Base1S4TestMarkObject
{
  var x: String = "ini";
}

// vim: set ts=4 sw=4 et:
