package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._

/**
 * Sister trait for <code> org.scalates.fixture.FunSuite </code> 
 * 
 * Example of usage: 
 * {{{
 * class MyFunSuite extends managedfixture.FunSuite[MyFixtureStateTypes.type]
 * {
 * 
 *   val fixtureStateTypes = MyFixtureStateTypes;
 *   val fixtureAccess = MyFixtureAccess;
 *
 *   import MyFixtureStateTypes.States._;
 *   
 *   fixtureUsage(start state(TWO) change(nothing))
 *   test("withDSL [4]: start state(TWO) change nothing") { x =>
 *      ....  
 *     assert(Base1S4TestMarkObject.x == "afterONE");
 *     Base1S4TestMarkObject.x = "afterTWO";
 *   }
 *
 *   fixtureUsage(start state(ONE) finish state(TWO))
 *   test("withDSL [4]: start state(ONE) finish state(TWO)") { x =>
 *      ....
 *      we know that we are in state ONE here.
 *   }
 *
 *   fixtureUsage(start state(TWO) finish state(undefined))
 *   test("withDSL [4]: start state(TWO) finish state(undefined)") { x =>
 *     // must be called after test with same state which change nothing.
 *     ........
 *   }
 *
 * }}}
 *
 **/
trait FunSuite[T <: ua.gradsoft.managedfixture.FixtureStateTypes] extends org.scalatest.fixture.FunSuite
                                           with AbstractManagedFixtureStateSuite[T]
{
  thisSuite =>

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
                                      // think: may be better complain ?
      neededFixtureStates(testName) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
      val nestedTestSuite = createNestedInstanceForTest(testName);
      // not needed - will be called during construction.
      //nestedTestSuite.test(testName, testTags: _* )(testFun);
      suitesToRun(testName) = nestedTestSuite;
    } else {
      if (testName == _parentTestName.get) {
        super.test(testName, testTags: _* )(testFun);
      }
    }
  }

  protected override def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
      // all ignred comt to top-level.
      super.ignore(testName, testTags:_*)(testFun);
    } 
  }
  

}

// vim: set ts=4 sw=4 et:
