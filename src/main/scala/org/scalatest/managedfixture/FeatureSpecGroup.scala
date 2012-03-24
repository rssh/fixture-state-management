package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._

abstract class FeatureSpecGroup[T <: FixtureStateTypes] extends managedfixture.FeatureSpec[T]
                                                           with SpecGroup
{

  execution autonomic
  
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    collectGrouped(classOf[FeatureSpec[T]]);
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker);
  }
  
  
  
}