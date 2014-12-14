package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._


abstract class FreeSpecGroup[T <: FixtureStateTypes] extends managedfixture.FreeSpec[T] 
                                              with SpecGroup
{

  execution autonomic
  
  override def run(testName: Option[String], args: Args): Status = {
    collectGrouped(this,classOf[managedfixture.FreeSpec[T]])
    internalSpec.run(testName, args)
  }
  
  
  
}
