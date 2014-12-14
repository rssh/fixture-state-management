package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._
import org.scalatest.fixture.NoArgTestWrapper


private[scalatest] class InternalPropSpec[T <: FixtureStateTypes](owner: managedfixture.PropSpec[T]) 
                                extends InternalSuite[T,managedfixture.PropSpec[T]](owner)
                                    with fixture.PropSpecLike
{

  def this() =
     this(InternalSuiteConstructorKluge.currentOwner.value.get.asInstanceOf[managedfixture.PropSpec[T]])
  
  def putTestWhenNested(specTest: String, tags: List[Tag], testFun: FixtureParam=>Any):Unit =
  {
    property(specTest, tags:_*)(testFun);   
  }
  
  def _property(testName: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
    if (!isNested) {
      setFixtureStateForTest(testName,testTags.toList,testFun)
    } else {
      property(testName,testTags:_*)(testFun);
    }
  }
  
  
  def _ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any): Unit = {
    ignore(testName, testTags:_*)(testFun)
  }
  
  def _info = info
   
  private[scalatest] override def fullTestName(text:String) = text;
  
  
}



/**
 * A sister trait to <code>org.scalatest.PropSpec</code> that can pass a managed fixture object into its tests.
 *
 *{{{
 *
 *import org.scalatest._
 *import org.scalates.managedfixture._
 *
 *class MySpects extends managedfixture.PropSpec[MyFixtureStateTypes]
 *{
 *
 *    val fixtureStateTypes = MyFixtureStateTypes;
 *    val fixtureAccess = MyFixtureStateAccess;
 *
 *     start state(ONE) change nothing 
 *     property("test") { f =>
 *       // here we know that S is in state one.
 *       assert(something)
 *     }
 *
 *
 *}
 *}}}
 *
 */
trait PropSpec[T <: ua.gradsoft.managedfixture.FixtureStateTypes] extends fixture.PropSpec
                                         with ExternalSuite[T]
                                         with Grouped
{ 

  
  lazy val internalSpec: InternalPropSpec[T] = createInternalSpec( (x:PropSpecGroup[T])=> x.internalSpec,
                                                                   new InternalPropSpec[T](this),
                                                                   classOf[PropSpecGroup[T]]         
                                                                 ) 
    
  protected override def property(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    internalSpec._property(testName, testTags: _*)(testFun)
  }

  protected override def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    internalSpec._ignore(testName, testTags: _*)(testFun);
  }
  
  implicit protected override def info: Informer = internalSpec._info

  override def run(testName: Option[String], args: Args): Status = {
      runGrouped(testName,  args,  internalSpec, classOf[PropSpecGroup[T]])
  }
  
  
  
}
