package ua.gradsoft.scalatest.fixture.managed

import org.scalatest._
import ua.gradsoft.testing._


trait FunSuite[T <: FixtureStateTypes] extends org.scalatest.fixture.FunSuite
                                           with AbstractManagedFixtureStateSuite[T]
{
  thisSuite =>

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
      neededFixtureStates(testName) = fixtureStateForNextTest;
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

  

}

// vim: set ts=4 sw=4 et:
