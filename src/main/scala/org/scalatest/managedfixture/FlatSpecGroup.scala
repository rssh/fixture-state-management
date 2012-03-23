package org.scalatest.managedfixture


import scala.util.DynamicVariable
import org.scalatest._
import ua.gradsoft.managedfixture._


abstract class FlatSpecGroup[T <: FixtureStateTypes] extends FlatSpec[T] 
                                                     with SpecGroup
{

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    collectGrouped(classOf[FlatSpec[T]]);
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker);
  }
  
}


