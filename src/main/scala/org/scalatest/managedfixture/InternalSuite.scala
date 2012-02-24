package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.testing._
import org.scalatest.fixture.NoArgTestWrapper;

import scala.util.DynamicVariable;

private[scalatest] trait ExternalSuite[T <: FixtureStateTypes] extends fixture.Suite
                                                                  with FixtureStateDSL[T]
{

  type FixtureStateTypes = T;

  type FixtureParam = T#FixtureType;

  /**
   * must be defined in subclass.
   **/
  def fixtureAccess: FixtureAccess[T]

  /**
   * must be defined in subclass
   **/
  def fixtureStateTypes: T

  protected def internalSpec : InternalSuite[T,_];

  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   internalSpec.fixtureUsage(value);
  }

  implicit protected def info: Informer = internalSpec._info

}

private[scalatest] abstract class InternalSuite[T <: FixtureStateTypes,
                                             S <: ExternalSuite[T] ](val owner: S)
                                         extends Suite
                                              with AbstractManagedFixtureStateSuite[T]
{

  def this() = 
   this(InternalSuiteConstructorKluge.currentOwner.value.get.asInstanceOf[S])

  def fixtureStateTypes = owner.fixtureStateTypes;
  def fixtureAccess = owner.fixtureAccess;

  def _info: Informer;

  var currentBranchName: Option[String] = None;

  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit ;
                         

  def setFixtureStateForTest(specText: String, tags: List[Tag], testFun: FixtureParam=>Any ) =
  {
    val testName = fullTestName(specText);
    neededFixtureStates(specText) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
    val nestedTestSuite = createNestedInstanceForTest(testName);
    nestedTestSuite.putTestWhenNested(specText, tags, testFun) ;
    suitesToRun(testName) = nestedTestSuite;
  }

  private[scalatest] def fullTestName(text:String) = currentBranchName.getOrElse("")+" "+text;

  override def createNestedInstanceForTest(testName:String) = {
    InternalSuiteConstructorKluge.currentOwner.withValue(Some(owner)){
        super.createNestedInstanceForTest(testName)
    }.asInstanceOf[this.type]
  }


}

private[scalatest] object InternalSuiteConstructorKluge
{
  val currentOwner = new DynamicVariable[Option[ExternalSuite[_]]](None);
}


