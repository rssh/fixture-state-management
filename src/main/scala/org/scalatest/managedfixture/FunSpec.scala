package org.scalatest.managedfixture

import org.scalatest._
import org.scalatest.events._
import org.scalatest.fixture.NoArgTestWrapper
import verb.BehaveWord

import ua.gradsoft.testing._

/**
 * internal trait where all expression are reevaluated
 **/
private[scalatest] class InternalFunSpec[T <: FixtureStateTypes](owner: FunSpec[T]) 
                                extends InternalSuite[T,FunSpec[T]](owner)
                                    with fixture.FunSpec
{

  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit =
  {
   _parent.get.asInstanceOf[InternalFunSpec[T]].currentBranchName match {
     case Some(x) => describe(x){ it(specTest,tags:_*)(testFun) } 
     case None => it(specTest,tags:_*)(testFun)
   }
  }


  def it_apply(specText: String, testTags: Tag*)(testFun: FixtureParam => Any):Unit = {
    if (isNested) {
       setFixtureStateForTest(specText, testTags.toList, testFun);
    } else {
       it(specText, testTags:_*)(testFun);
    }
  }

  def _ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
      ignore(specText, testTags:_*)(testFun);
  }

  def _describe(description: String)(fun: => Unit):Unit = {
      currentBranchName=Some(description);
      describe(description)(fun);
  }

  def _info = info;

}



/**
 * A sister trait to <code>org.scalatest.FunSpec</code> that can pass a managed fixture object into its tests.
 *
 * {{{
 *  class MyFunSpec extends managedfixture.FunSpec[StackStateTypes]
 *  {
 *    val fixtureStateTypes = StackStateTypes
 *    val fixtureAccess = StackAccess
 *
 *    describe("A Stack") {
 *
 *      start state(nonEmpty) finish state(undefined)
 *      it "shoud pop a value" { stack =>
 *         val x = stack.pop
 *      }
 *
 *      start state(empty) change(nothing)
 *      it "shoud not pop a value" { stack =>
 *         intercept[NoSuchElementException] {
 *           val x = stack.pop
 *         }
 *      }
 *      
 *
 *    }
 *
 *  }
 *
 * }}}
 *
 */
trait FunSpec[T <: FixtureStateTypes] extends fixture.Suite
                                          with ExternalSuite[T]
{ 

  protected override val internalSpec = new InternalFunSpec[T](this);

  protected final class ItWord {

    def apply(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
      internalSpec.it_apply(specText, testTags:_*)(testFun)
    }

    def should(behaveWord: BehaveWord) = behaveWord

    def must(behaveWord: BehaveWord) = behaveWord
  }

  protected val it = new ItWord

  protected def ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    internalSpec._ignore(specText, testTags:_*)(testFun)
  }

  protected def ignore(specText: String)(testFun: FixtureParam => Any) {
    internalSpec._ignore(specText, Array[Tag]():_*)(testFun)
  }

  protected def describe(description: String)(fun: => Unit) {
    internalSpec._describe(description)(fun)
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
  }

  protected val behave = new BehaveWord

  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): FixtureParam => Any = {
    fixture => f
  }

  protected implicit def convertNoArgToFixtureFunction(fun: () => Any): (FixtureParam => Any) =
    new NoArgTestWrapper(fun)
}
