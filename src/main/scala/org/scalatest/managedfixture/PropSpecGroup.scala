package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._


abstract class PropSpecGroup[T <: FixtureStateTypes] extends managedfixture.PropSpec[T] 
                                                        with SpecGroup
{

  execution autonomic

  override def run(testName: Option[String], args: Args): Status = 
  {
    collectGrouped(this,classOf[FlatSpec[T]])
    internalSpec.run(testName, args)
  }
    
  
  
}
