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

private[scalatest] object FeatureSpecConstructorKluge
{
  val currentOwner = new DynamicVariable[Option[FeatureSpec[_]]](None);
}

/**
 * sister trait for [[org.scalatest.fixture.FeatureSpec]]
 *
 * Let's translate example from original scalatest scaladoc:
 *{{{
 *class StackSpec extends managedfixture.FeatureSpec[StackStateTypes]
 *{
 *  val fixtureStateTypes ...
 *  val fixtureAccess = ...
 *
 *  feature("pushing a value onto a stack") {
 *
 *     start state(any) finish state(nonEmpty)
 *     scenario("User pashes a value") { stack =>
 *        stack.push(9)
 *        assert(stack.head == 9)
 *     }
 *
 *  }
 *
 *  feature("Popping a value off a stack") {
 *
 *     start state(nonEmpty) finish state(undefined)
 *     scenario("User pops a value from nonEmpty stack") { stack =>
 *        val s1 = stack.size;
 *        val top = stack.pop;
 *        assert(stack.size == s1-1);
 *     }
 * 
 *     start state(empty) change nothing 
 *     scenario("User pops a value from empty stack") { stack =>
 *       intercept[NoSuchElementException] {
 *           (new Stack[Int]).pop()
 *       }
 *     }
 *
 *  }
 *
 *}
 *
 *}}}
 */
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

