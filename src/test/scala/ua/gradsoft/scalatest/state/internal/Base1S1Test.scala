package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class Base1S1Test extends fixture.FunSuite
{

  val stateInfo = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  type FST = Base1FixtureStateInfo.type;

  val stateManager = new FixtureStateManager[FST](fixtureAccess);

  val fixtureStateData = FixtureStateUsageDescription[FST](stateInfo).withAnyState;

  val dummyStateData = FixtureStateUsageDescription[FST](stateInfo).withAnyState;

  val testStateUsageDescriptions : MutableMap[String, FixtureStateUsageDescription[FST]] = LinkedHashMap();

  type FixtureParam = FST#FixtureType;

  def withFixture(test: OneArgTest) =
  {
    // 
    val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    stateManager.doWith(x,test);
  }

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    super.test(testName, testTags:_*)(testFun);
    //registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", sourceFileName, "test", stackDepth, testTags: _*)
  }


  
  test("test nothing to start") { () =>
    assert(true);
  }

  import Base1FixtureStateInfo.States._;

  val testName = "receive state"
  testStateUsageDescriptions(testName)=FixtureStateUsageDescription.apply[FST](stateInfo).
                                                      withStartState(TWO);
  test(testName) { x =>
    assert(x==2);
  }


}



// vim: set ts=4 sw=4 et:
