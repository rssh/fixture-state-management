package org.scalatest.managedfixture


import scala.util.DynamicVariable
import org.scalatest._
import ua.gradsoft.managedfixture._


abstract class FlatSpecGroup[T <: FixtureStateTypes] extends managedfixture.FlatSpec[T] 
                                                     with SpecGroup
{
  
  execution autonomic

  override def run(testName: Option[String], args: Args): Status = 
  {
    collectGrouped(this,classOf[FlatSpec[T]])
    internalSpec.run(testName, args)
  }
  
}


