package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._

abstract class FeatureSpecGroup[T <: FixtureStateTypes] extends managedfixture.FeatureSpec[T]
                                                           with SpecGroup
{

  execution autonomic
  
  override def run(testName: Option[String], args: Args): Status = 
  {
    collectGrouped(this,classOf[FeatureSpec[T]])
    internalSpec.run(testName, args)
  }
  
  
  
}
