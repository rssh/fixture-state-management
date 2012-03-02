package org.scalatest.managedfixture

import org.scalatest._
import org.scalatest.verb.BehaveWord
import org.scalatest.events._
import org.scalatest.fixture.NoArgTestWrapper;


import ua.gradsoft.testing._

import scala.util.DynamicVariable;


private[scalatest] class InternalFreeSpec[T <: FixtureStateTypes](owner: FreeSpec[T]) 
                                            extends InternalSuite[T, FreeSpec[T]](owner)
                                             with fixture.FreeSpec
{

  def this() =
    this(InternalSuiteConstructorKluge.currentOwner.value.get.asInstanceOf[FreeSpec[T]])


  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit =
  {
   _parent.get.asInstanceOf[InternalFreeSpec[T]].currentBranchName match {
     case Some(x) => x - { taggedInvocationOnString_in(specTest,tags,testFun) }
     case None => "" - { taggedInvocationOnString_in(specTest,tags,testFun) }
   }
  }


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
     val sv = currentBranchName;
     if (currentBranchName == None) {
        currentBranchName = Some(description)
     } else {
        currentBranchName = Some(currentBranchName.get+" "+description)
     }
     description - branch
     currentBranchName = sv;
   }

  def _info = info;



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
 *         "shoud not find any user in empty db" in { db =>
 *           inTransaction{ 
 *               assert (db.findUserByName("Jon").isEmpty)
 *           } 
           }
 *
 *         start state(DATASET1) change nothing
 *         "shoud find user Jon in dataset 1" in { db =>
 *           inTransaction{ 
 *               assertNot (db.findUserByName("Jon").isEmpty)
 *           }
           }
 *
 *     }
 *
 *
 *  }
 *}}}
 *@see [[ua.gradsoft.testing]], [[org.scalatest.managedsuite]]
 */
trait FreeSpec[T <: FixtureStateTypes] extends fixture.Suite 
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

  override final def withFixture(test: OneArgTest): Unit =
          throw new IllegalStateException("You can't call withFixture diretly in managedfixture");

  protected lazy val internalSpec = new InternalFreeSpec[T](this);

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


