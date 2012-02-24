package org.scalatest.managedfixture

import ua.gradsoft.testing._
import org.scalatest._
import org.scalatest.fixture.NoArgTestWrapper


/**
 * A sister trait to <code>org.scalatest.PropSpec</code> that can pass a managed fixture object into its tests.
 *
 *{{{
 *
 *import org.scalatest._
 *import ua.gradsoft.testing._
 *
 *class MySpects extends managedfixture.PropSpec[MyFixtureStateTypes]
 *{
 *
 *    val fixtureStateTypes = MyFixtureStateTypes;
 *    val fixtureAccess = MyFixtureStateAccess;
 *
 *     start state(ONE) change nothing 
 *     property("test") { f =>
 *       // here we know that S is in state one.
 *       assert(something)
 *     }
 *
 *
 *}
 *}}}
 *
 */
trait PropSpec[T <: FixtureStateTypes] extends fixture.PropSpec
                                         with AbstractManagedFixtureStateSuite[T]
{ 

  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   fixtureUsage(value);
  }

  protected override def property(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {   
      neededFixtureStates(testName) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
      val nestedTestSuite = createNestedInstanceForTest(testName);
      // not needed - will be called during construction.
      //nestedTestSuite.property(testName, testTags: _* )(testFun);
      suitesToRun(testName) = nestedTestSuite;
    } else {
      if (testName==_parentTestName.get) {
        super.property(testName, testTags:_*)(testFun);
      }
    }
  }

  protected override def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
      super.ignore(testName, testTags: _*)(testFun);
    }
  }

}
