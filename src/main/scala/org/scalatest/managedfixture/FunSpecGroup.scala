package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._

abstract class FunSpecGroup[T <: FixtureStateTypes] extends managedfixture.FunSpec[T]
                                                     with SpecGroup
{

  execution autonomic
  
  override def run(testName: Option[String], args: Args): Status =
  {
    collectGrouped(this,classOf[managedfixture.FunSpec[T]]) 
    internalSpec.run(testName, args)
  }
    
  
}
