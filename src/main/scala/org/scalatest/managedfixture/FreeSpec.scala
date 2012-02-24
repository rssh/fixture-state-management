package org.scalatest.managedfixture

import org.scalatest._
import org.scalatest.verb.BehaveWord
import org.scalatest.events._
import org.scalatest.fixture.NoArgTestWrapper;


import ua.gradsoft.testing._

import scala.util.DynamicVariable;


private[scalatest] class InternalFreeSpec[T <: FixtureStateTypes](val owner: FreeSpec[T]) 
                                                                      extends fixture.FreeSpec
                                                                       with AbstractManagedFixtureStateSuite[T]
{

  def this() =
    this( FreeSpecConstructorKluge.currentOwner.value.get.asInstanceOf[FreeSpec[T]] )

  def fixtureStateTypes = owner.fixtureStateTypes;
  def fixtureAccess = owner.fixtureAccess;

  var currentBranchName: Option[String] = None;

  def setFixtureStateForTest(specText:String, tags: List[Tag], testFun: FixtureParam=>Any ): Unit =
  {
    val testName = fullTestName(specText);
    neededFixtureStates(testName) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
    val nestedTestSuite = createNestedInstanceForTest(testName);
    currentBranchName.foreach(
       nestedTestSuite.stringWrapper_minus(_, {
           nestedTestSuite.taggedInvocationOnString_in(specText,tags,testFun);
         }
       )
    );
    suitesToRun(testName) = nestedTestSuite;
  }

  
  private[scalatest] def fullTestName(text:String) = currentBranchName.getOrElse("")+" "+text;

  def taggedInvocationOnString_in(specText: String, tags: List[Tag], testFun: FixtureParam => Any): Unit = 
   {
     if (!isNested) { 
       setFixtureStateForTest(specText, tags, testFun);
     } else {
       (new ResultOfTaggedAsInvocationOnString(specText, tags)).in(testFun) 
     }
   }

  def taggedInvocationOnString_is(specText: String, tags: List[Tag], testFun: => PendingNothing) = 
   {
     (new ResultOfTaggedAsInvocationOnString(specText, tags)).is(testFun) 
   }

  def taggedInvocationOnString_ignore(specText: String, tags: List[Tag], testFun: FixtureParam => Any) = 
   {
     (new ResultOfTaggedAsInvocationOnString(specText, tags)).ignore(testFun) 
   }

  def stringWrapper_minus(description: String, branch: => Unit) = 
   {
     currentBranchName = Some(description)
     description - branch
   }

  def _info = info;

  override def createNestedInstanceForTest(testName: String) =
  {
    FreeSpecConstructorKluge.currentOwner.withValue(Some(owner)){
        super.createNestedInstanceForTest(testName)
    }.asInstanceOf[InternalFreeSpec[T]];
  }


}



/**
 * A sister trait to <code>org.scalatest.FreeSpec</code> that can pass a managed fixture object into its tests.
 *
 *{{{
 *  class MyFreeSpec extends managedfixture.FreeSpec[MyFixtureStateTypes]
 *  {
 *     val fixtureStateTypes = MyFixtureStateTypes;
 *     val fixtureAccess = MyFixtureAccess;
 *
 *     "A system" - {
 *
 *         start state(INITIAL) change nothing
 *         "shoud not find any user in empty db" in 
 *           inTransaction{ 
 *               assert (db.findUserByName("Jon").isEmpty)
 *           } 
 *
 *         start state(DATASET1) change nothing
 *         "shoud find user Jon in dataset 1" in 
 *           inTransaction{ 
 *               assertNot (db.findUserByName("Jon").isEmpty)
 *           }
 *
 *     }
 *
 *
 *  }
 *}}}
 *@see [[ua.gradsoft.testing]], [[org.scalatest.managedsuite]]
 */
trait FreeSpec[T <: FixtureStateTypes] extends Suite 
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

  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   internalSpec.fixtureUsage(value);
  }

  lazy val internalSpec = new InternalFreeSpec[T];

  implicit protected def info: Informer = internalSpec._info;

  protected final class ResultOfTaggedAsInvocationOnString(specText: String, tags: List[Tag]) {

    def in(testFun: FixtureParam => Any) {
      internalSpec.taggedInvocationOnString_in(specText, tags, testFun);
      //registerTestToRun(specText, "in", tags, testFun)
    }

    def in(testFun: () => Any) {
      internalSpec.taggedInvocationOnString_in(specText, tags, new NoArgTestWrapper(testFun));
      //registerTestToRun(specText, "in", tags, new NoArgTestWrapper(testFun))
    }

    def is(testFun: => PendingNothing) {
      internalSpec.taggedInvocationOnString_is(specText, tags, testFun);
    }

    def ignore(testFun: FixtureParam => Any) {
      internalSpec.taggedInvocationOnString_ignore(specText, tags, testFun);
    }

    def ignore(testFun: () => Any) {
      internalSpec.taggedInvocationOnString_ignore(specText, tags, new NoArgTestWrapper(testFun));
    }

  }

  protected final class FreeSpecStringWrapper(string: String) {

    def - (fun: => Unit) {
      internalSpec.stringWrapper_minus(string,fun);
      //registerNestedBranch(string, None, fun, "describeCannotAppearInsideAnIt", sourceFileName, "-", stackDepth - 1)
    }

    def in(testFun: FixtureParam => Any) {
      internalSpec.taggedInvocationOnString_in(string, List(), testFun);
      //registerTestToRun(string, "in", List(), testFun)
    }

    def in(testFun: () => Any) {
      internalSpec.taggedInvocationOnString_in(string, List(), new NoArgTestWrapper(testFun));
      //registerTestToRun(string, "in", List(), new NoArgTestWrapper(testFun))
    }

    def is(testFun: => PendingNothing) {
      internalSpec.taggedInvocationOnString_is(string, List(), testFun);
      //registerTestToRun(string, "is", List(), unusedFixtureParam => testFun)
    }

    def ignore(testFun: FixtureParam => Any) {
      internalSpec.taggedInvocationOnString_ignore(string, List(), testFun);
      //registerTestToIgnore(string, "ignore", List(), testFun)
    }

    def ignore(testFun: () => Any) {
      internalSpec.taggedInvocationOnString_ignore(string, List(), new NoArgTestWrapper(testFun));
      //registerTestToIgnore(string, "ignore", List(), new NoArgTestWrapper(testFun))
    }

    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ResultOfTaggedAsInvocationOnString(string, tagList)
    }
  }

  protected implicit def convertToFreeSpecStringWrapper(s: String) = new FreeSpecStringWrapper(s)

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
  }

  protected val behave = new BehaveWord
}

private[scalatest] object FreeSpecConstructorKluge
{
 val currentOwner = new DynamicVariable[Option[FreeSpec[_]]](None);
}

