package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._


abstract class FreeSpecGroup[T <: FixtureStateTypes] extends managedfixture.FreeSpec[T] 
                                              with SpecGroup
{

  execution autonomic
  
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    collectGrouped(this,classOf[managedfixture.FreeSpec[T]]);
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker);
  }
  
  
  
}
