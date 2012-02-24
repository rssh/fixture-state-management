package org.scalatest.managedfixture

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.util.DynamicVariable;

import org.scalatest._;
import ua.gradsoft.testing._;


/**
 * Abstract Suite for fixtures with managed states. Idea that before each test we
 * describe state, needed for this fixture in micro-dsl and provide operations for
 * load  such state.
 *  Than engine will prepare state before calling each test, according to given description
 * and will execute states in order which minimize number of state loads.
 * 
 * Typical usecase for such scenario -- where fixture is a relational database which 
 * must situated in given state.
 **/
private[scalatest] trait AbstractManagedFixtureStateSuite[T <: FixtureStateTypes] extends org.scalatest.fixture.Suite
                                                               with FixtureStateDSL[T]
{

  type FixtureStateTypes = T;
  type FixtureParam = T#FixtureType;

  /**
   * Must be defined in subclass.
   **/
  def fixtureAccess: FixtureAccess[T]

  /**
   * must be defined in subclass.
   **/
  def fixtureStateTypes: T 

  protected def fixtureStateManager: FixtureStateManager[T] = if (isNested) {
                                        _parent.get.fixtureStateManager
                                      } else {
                                         _fixtureStateManager 
                                      }


  private lazy val _fixtureStateManager = new FixtureStateManager[T](fixtureAccess);

  protected def neededFixtureStates: MutableMap[String, TestFixtureStateUsageDescription[T]] = 
                                      if (isNested) {
                                        _parent.get.neededFixtureStates
                                      } else {
                                        _neededFixtureStates
                                      }

  private lazy val _neededFixtureStates: MutableMap[String,TestFixtureStateUsageDescription[T]] =
                                                                                         LinkedHashMap();

  override def nestedSuites = suitesToRun.values.toList;
  protected lazy val suitesToRun: MutableMap[String,Suite] = MutableMap[String,Suite]();

  private[scalatest] lazy val defaultFixtureState = TestFixtureStateUsageDescription[T](fixtureStateTypes);
  private[scalatest] var fixtureStateForNextTest:Option[TestFixtureStateUsageDescription[T]] = None;
  

  def withFixture(test: OneArgTest) =
  {
    val x = neededFixtureStates.get(test.name).getOrElse(defaultFixtureState);
    fixtureStateManager.doWith(x, test);
  }

  def fixtureUsage(dsl:DSLExpression):Unit = 
    { fixtureStateForNextTest = Some(dsl.value); }

  def fixtureUsage(usage: TestFixtureStateUsageDescription[T]):Unit = 
    { fixtureStateForNextTest = Some(usage); }


  protected def isNested : Boolean = (_parent != None);

  protected var _parent : Option[AbstractManagedFixtureStateSuite[T]] = {
           ManagedFixtureStateSuiteConstructorKluge.currentParent.value map { x =>
                ManagedFixtureStateSuiteConstructorKluge.currentParent.value=None;
                x.asInstanceOf[AbstractManagedFixtureStateSuite[T]];
           }
  }

  protected var _parentTestName : Option[String] = {
           ManagedFixtureStateSuiteConstructorKluge.currentTestName.value map { x =>
              ManagedFixtureStateSuiteConstructorKluge.currentTestName.value=None;
              x;
           }
  }

  protected def createNestedInstanceForTest(testName:String) = {
    ManagedFixtureStateSuiteConstructorKluge.currentParent.withValue(Some(this)){
      ManagedFixtureStateSuiteConstructorKluge.currentTestName.withValue(Some(testName)){
         this.getClass.newInstance.asInstanceOf[AbstractManagedFixtureStateSuite[T]];
      }
    }
  }

  protected override def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any],
                                distributor: Option[Distributor], tracker: Tracker)=
  {
   if (!isNested) {
    // run subsuites in terms of order
    val sequenceParts = ExecutionSequenceOptimizer.optimizeOrder(neededFixtureStates);
    val optLock = fixtureAccess.suiteLevelLock;
    optLock.foreach( _.acquire() )
    try {
     for(l <- sequenceParts) {
      if (l.size == 1 || distributor == None) {
       // must be run without distributor
       for(nested <- l) {
          // TODO: think about stopRequested
          suitesToRun(nested).run(None,reporter,stopper,filter,configMap,distributor,tracker);
       }
      } else {
       // start this testes in parallel
       for(nested <- l) {
         distributor.get.apply(suitesToRun(nested),tracker);
       }
      }
     }
    } finally {
      optLock.foreach( _.release() )
    }
   } else {
     super.runNestedSuites(reporter, stopper, filter, configMap, distributor, tracker);
   }
  }


}


// used to pass parameter to nested constructor (since we have
// test/describe methods, called during initialization, then we can't set nested
// parameters after init: it's too late)
private[scalatest] object ManagedFixtureStateSuiteConstructorKluge
{

  val currentParent = new DynamicVariable[Option[AbstractManagedFixtureStateSuite[_]]](None);
  val currentTestName = new DynamicVariable[Option[String]](None);

}




// vim: set ts=4 sw=4 et:
