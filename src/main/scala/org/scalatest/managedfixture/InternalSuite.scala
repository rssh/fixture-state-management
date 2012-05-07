package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._
import org.scalatest.fixture.NoArgTestWrapper;

import scala.util.DynamicVariable;

private[scalatest] trait ExternalSuite[T <: ua.gradsoft.managedfixture.FixtureStateTypes] extends fixture.Suite
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
  
  override def withFixture(test: OneArgTest): Unit =
          throw new IllegalStateException("You can't call withFixture diretly in managedfixture");
  
  

}

private[scalatest] abstract class InternalSuite[T <: ua.gradsoft.managedfixture.FixtureStateTypes,
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
    neededFixtureStates(testName) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
    val nestedTestSuite = createNestedInstanceForTest(testName);
    nestedTestSuite.putTestWhenNested(specText, tags, testFun) ;
    suitesToRun(testName) = nestedTestSuite;
    if (fixtureStateForNextTest.isEmpty) {
       throw new IllegalStateException("state for spec "+specText+" is not set");
    }
  }

  private[scalatest] def fullTestName(text:String) = (currentBranchName match {
                                                        case Some(branchText) => branchText+" "+text
                                                        case None => text
                                                     }).trim


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


