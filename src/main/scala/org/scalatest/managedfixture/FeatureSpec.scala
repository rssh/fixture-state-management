package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._
import org.scalatest.fixture.NoArgTestWrapper;

import scala.util.DynamicVariable;

private[scalatest] class InternalFeatureSpec[T <: FixtureStateTypes](owner: FeatureSpec[T])
                                     extends InternalSuite[T,FeatureSpec[T]](owner)
                                         with fixture.FeatureSpec
{

  def this() = 
   this(InternalSuiteConstructorKluge.currentOwner.value.get.asInstanceOf[FeatureSpec[T]])

  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit =
  {
   _parent.get.asInstanceOf[InternalFeatureSpec[T]].currentBranchName match {
     case Some(x) => feature(x){ scenario(specTest,tags:_*)(testFun) }
     case None => scenario(specTest,tags:_*)(testFun)
   }
  }

 private[scalatest] override def fullTestName(text:String) = 
                          currentBranchName.getOrElse("")+" Scenario: "+text;



  def _info: Informer = info

  

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
     currentBranchName = Some(description); 
     feature(description)(fun); 
  }
 
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
trait FeatureSpec[T <: FixtureStateTypes] extends fixture.Suite
                                          with ExternalSuite[T]
{


  /**
   * Must be defined in subclass.
   **/
  def fixtureAccess: FixtureAccess[T]

  /**
   * must be defined in subclass.
   **/
  def fixtureStateTypes: T

  
  protected override lazy val internalSpec: InternalFeatureSpec[T] = new InternalFeatureSpec[T](this);
                                              

  override def withFixture(test: OneArgTest): Unit =
          throw new IllegalStateException("You can't call withFixture diretly in managedfixture");


  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   internalSpec.fixtureUsage(value);
  }

  protected def scenario(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) =
  {
    internalSpec._scenario(specText, testTags:_*)(testFun);
  }

  protected def ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) = {
    internalSpec._ignore(specText, testTags:_*)(testFun);
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

