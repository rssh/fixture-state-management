package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._


private[scalatest] class InternalFunSuite[T <: FixtureStateTypes](owner: managedfixture.FunSuite[T]) 
                                extends InternalSuite[T,managedfixture.FunSuite[T]](owner)
                                    with fixture.FunSuiteLike
{

  def this() =
     this(InternalSuiteConstructorKluge.currentOwner.value.get.asInstanceOf[FunSuite[T]])
  
  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit =
  {
    test(specTest, tags: _* )(testFun);   
  }
  
  def _test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
    if (!isNested) {
      setFixtureStateForTest(testName,testTags.toList,testFun)
    } else {
      test(testName,testTags:_*)(testFun);
    }
  }
  
  
  def _ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
    ignore(testName, testTags:_*)(testFun)
  }
  
  def _info = info
   
  private[scalatest] override def fullTestName(text:String) = text;
  
  
}

/**
 * Sister trait for <code> org.scalates.fixture.FunSuite </code> 
 * 
 * Example of usage: 
 * {{{
 * class MyFunSuite extends managedfixture.FunSuite[MyFixtureStateTypes.type]
 * {
 * 
 *   val fixtureStateTypes = MyFixtureStateTypes;
 *   val fixtureAccess = MyFixtureAccess;
 *
 *   import MyFixtureStateTypes.States._;
 *   
 *   fixtureUsage(start state(TWO) change(nothing))
 *   test("withDSL [4]: start state(TWO) change nothing") { x =>
 *      ....  
 *     assert(Base1S4TestMarkObject.x == "afterONE");
 *     Base1S4TestMarkObject.x = "afterTWO";
 *   }
 *
 *   fixtureUsage(start state(ONE) finish state(TWO))
 *   test("withDSL [4]: start state(ONE) finish state(TWO)") { x =>
 *      ....
 *      we know that we are in state ONE here.
 *   }
 *
 *   fixtureUsage(start state(TWO) finish state(undefined))
 *   test("withDSL [4]: start state(TWO) finish state(undefined)") { x =>
 *     // must be called after test with same state which change nothing.
 *     ........
 *   }
 *
 * }}}
 *
 **/
trait FunSuite[T <: FixtureStateTypes] extends fixture.FunSuite
                                           with ExternalSuite[T]
                                           with Grouped
{
   

  lazy val internalSpec: InternalFunSuite[T] = createInternalSpec(((x:FunSuiteGroup[T]) => x.internalSpec),
                                                                   new InternalFunSuite[T](this),
                                                                   classOf[FunSuiteGroup[T]]);
    
  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
    internalSpec._test(testName, testTags:_*)(testFun)
  }
    
  protected override def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any):Unit = {
    internalSpec._ignore(testName, testTags:_*)(testFun);
  }

  implicit protected override def info: Informer = internalSpec._info

  override def run(testName: Option[String], args: Args): Status = 
        runGrouped(testName, args,  internalSpec, classOf[FunSuiteGroup[T]])
  
  
}

// vim: set ts=4 sw=4 et:
