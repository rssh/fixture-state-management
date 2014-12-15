package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import org.scalatest.OutcomeOf._
import ua.gradsoft.managedfixture._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class Base1S2Test extends fixture.FunSuite
                             with FixtureStateDSL[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  type FST = Base1FixtureStateInfo.type;

  val stateManager = new FixtureStateManager[FST](fixtureAccess);
  val dummyStateData = TestFixtureStateUsageDescription[FST](fixtureStateTypes).withAnyState;

  type FixtureParam = FST#FixtureType;
  var currentFixtureData = TestFixtureStateUsageDescription[FST](fixtureStateTypes);

  val testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[FST]] = LinkedHashMap();

  def fixtureUsage(x: DSLExpression) =
  {  currentFixtureData = x.value; }


  def withFixture(test: OneArgTest): Outcome =
  {
    // 
    val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    outcomeOf{ stateManager.doWith(x, test) }
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
    fixtureAccess.set(TWO);    
  }


}



// vim: set ts=4 sw=4 et:
