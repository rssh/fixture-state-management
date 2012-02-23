package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.testing._
import org.scalatest.fixture.NoArgTestWrapper;

import scala.util.DynamicVariable;

private[scalatest] class InternalFeatureSpec[T <: FixtureStateTypes](val owner: FeatureSpec[T])
                                         extends fixture.FeatureSpec
                                              with AbstractManagedFixtureStateSuite[T]
{

  def this() = 
   this(FeatureSpecConstructorKluge.currentOwner.value.get.asInstanceOf[FeatureSpec[T]])

  def fixtureStateTypes = owner.fixtureStateTypes;
  def fixtureAccess = owner.fixtureAccess;

  def _info: Informer = info

  def currentFeature: Option[String] = None;

  def setFixtureStateForTest(specText: String, tags: List[Tag], testFun: FixtureParam=>Any ) =
  {
    val testName = fullTestName(specText);
    neededFixtureStates(specText) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
    val nestedTestSuite = createNestedInstanceForTest(testName);
    currentFeature.foreach(
      nestedTestSuite.feature(_) {
        () => nestedTestSuite.scenario(specText,tags:_*)(testFun);
      }
    );
    suitesToRun(testName) = nestedTestSuite;
  }

  def _scenario(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
       setFixtureStateForTest(specText, testTags.toList, testFun);
    } else {
       super.scenario(specText, testTags:_*)(testFun);
    }
  }

  def _ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) = 
  {  ignore(specText, testTags:_*)(testFun); }


  def _feature(description: String)(fun: => Unit) = {
     feature(description)(fun); 
  }
 
  private[scalatest] def fullTestName(text:String) = currentFeature.getOrElse("")+" "+text;

  override def createNestedInstanceForTest(testName:String) = {
    FeatureSpecConstructorKluge.currentOwner.withValue(Some(owner)){
        super.createNestedInstanceForTest(testName)
    }.asInstanceOf[InternalFeatureSpec[T]]
  }

}

object FeatureSpecConstructorKluge
{
  val currentOwner = new DynamicVariable[Option[FeatureSpec[_]]](None);
}

trait FeatureSpec[T <: FixtureStateTypes] extends Suite
                                          with FixtureStateDSL[T]
{

  type FixtureStateTypes = T;
  type FixtureParam = T#FixtureType;

  /**
   * Must be defined in subclass.
   **/
  def fixtureAccess: FixtureAccess[T]

  /**
   * must be defined in subclass.
   **/
  def fixtureStateTypes: T

  private lazy val internalSpec = new InternalFeatureSpec[T](this);

  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   internalSpec.fixtureUsage(value);
  }

  implicit protected def info: Informer = internalSpec._info

  protected def scenario(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) =
  {
    internalSpec._scenario(specText, testTags:_*)(testFun);
  }

  protected def scenario(specText: String, testTags: Tag*)(testFun: () => Any) =
  {
    internalSpec._scenario(specText, testTags:_*)(new NoArgTestWrapper(testFun));
  }

  protected def ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) = {
    internalSpec._ignore(specText, testTags:_*)(testFun);
  }

  protected def ignore(specText: String, testTags: Tag*)(testFun: () => Any) = {
    internalSpec._ignore(specText, testTags:_*)(new NoArgTestWrapper(testFun));
  }

  protected def feature(description: String)(fun: => Unit) {
    internalSpec._feature(description)(fun);
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
     internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
  }

  protected def scenariosFor(unit: Unit) {}

  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): FixtureParam => Any = {
    fixture => f
  }

}

