package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class Base1S1Test extends fixture.FunSuite
{

  val stateInfo = Base1FixtureStateInfo;
  val stateOps = Base1FixtureStateOperations;

  type FST = Base1FixtureStateInfo.type;

  val stateManager = new FixtureStateManager[FST](stateOps);

  val fixtureStateData = TestFixtureStateUsageDescription[FST](stateInfo).withAnyState;

  val dummyStateData = TestFixtureStateUsageDescription[FST](stateInfo).withAnyState;

  val testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[FST]] = LinkedHashMap();

  type FixtureParam = FST#FixtureType;

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
    System.err.println("registeting test: "+testName);
    super.test(testName, testTags:_*)(testFun);
    //registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", sourceFileName, "test", stackDepth, testTags: _*)
  }


  
  test("test nothing to start") { () =>
    assert(true);
  }

  import Base1FixtureStateInfo.States._;

  val testName = "receive state"
  testStateUsageDescriptions(testName)=TestFixtureStateUsageDescription.apply[FST](stateInfo).
                                                      withStartState(TWO);
  test(testName) { x =>
    assert(x==2);
  }


}



// vim: set ts=4 sw=4 et:
