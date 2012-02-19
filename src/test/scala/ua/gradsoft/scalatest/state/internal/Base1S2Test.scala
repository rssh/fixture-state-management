package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class Base1S2Test extends fixture.FunSuite
                             with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val stateOps = Base1FixtureStateOperations;

  type FST = Base1FixtureStateInfo.type;

  val stateManager = new FixtureStateManager[FST](stateOps);
  val dummyStateData = TestFixtureStateUsageDescription[FST](fixtureStateTypes).withAnyState;

  type FixtureParam = FST#FixtureType;
  var currentFixtureData = TestFixtureStateUsageDescription[FST](fixtureStateTypes);

  val testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[FST]] = LinkedHashMap();

  def fixtureUsage(x: DSLExpression) =
  {  currentFixtureData = x.value; }


  def withFixture(test: OneArgTest)
  {
    // 
    val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    stateManager.doWith(x.precondition,
                        x.stateAspectsChanged,
                        x.startStateChange,
                        test);
  }

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    testStateUsageDescriptions(testName) = currentFixtureData;
    super.test(testName, testTags:_*)(testFun);
    //registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", sourceFileName, "test", stackDepth, testTags: _*)
  }


  import Base1FixtureStateInfo.States._;

  fixtureUsage(start state(TWO) change(nothing))
  test("witDSL: start state(TWO) change nothing") { x =>
    assert(x==2);
  }

  fixtureUsage(start state(ONE) finish state(TWO))
  test("withDSL: start state(ONE) finish state(TWO)") { x =>
    assert(x==1);
    stateOps.set(TWO);    
  }


}



// vim: set ts=4 sw=4 et:
