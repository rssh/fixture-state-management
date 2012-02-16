package ua.gradsoft.scalates.state.internal

import org.scalatest._
import ua.gradsoft.scalatest.state._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;


class Base1SimpleTest extends fixture.FunSuite
{

  val stateInfo = Base1FixtureStateInfo;

  val stateManager = new FixtureStateManager[Base1FixtureStateInfo](stateInfo);


  val fixtureStateData = new TestFixtureStateUsageDescription[Base1FixtureStateInfo](stateInfo) {
    def precondition = new AnyState(fixtureStateInfo);
  };

  val dummyStateData = new TestFixtureStateUsageDescription[Base1FixtureStateInfo](stateInfo) {
    def precondition = new AnyState(fixtureStateInfo);
  };

  val testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[Base1FixtureStateInfo]] = LinkedHashMap();

  type FixtureParam = Base1FixtureStateInfo#FixtureType;

  def withFixture(test: OneArgTest)
  {
    // 
    //val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    val x = testStateUsageDescriptions.get(test.name) match {
              case Some(x) => System.out.println("found description:"+x+" for name "+test.name);
                            x;
              case None => System.out.println("nothing found for name "+test.name);
                           dummyStateData;
    }
                       
    stateManager.doWith(x.precondition,
                        x.stateAspectsChanged,
                        x.startStateChange,
                        test);
  }

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    System.err.println("our test");
    super.test(testName, testTags:_*)(testFun);
    //registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", sourceFileName, "test", stackDepth, testTags: _*)
  }


  
  test("test nothing to start") { x =>
    assert(true);
  }

  import Base1FixtureStateInfo.States._;

  val testName = "receive state"
  testStateUsageDescriptions(testName)=new TestFixtureStateUsageDescription[
                                                      Base1FixtureStateInfo](stateInfo) {
    def precondition = new SetOfStatesAndAspects[Base1FixtureStateInfo](fixtureStateInfo,
                                                                        Set(TWO),
                                                                        Set());
  }
  test(testName) { x =>
    assert(x==2);
  }


}



// vim: set ts=4 sw=4 et:
