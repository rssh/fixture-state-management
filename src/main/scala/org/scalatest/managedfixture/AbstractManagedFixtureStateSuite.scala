package org.scalatest.managedfixture

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.util.DynamicVariable;

import org.scalatest._;
import ua.gradsoft.managedfixture._;


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
private[scalatest] trait AbstractManagedFixtureStateSuite[T <: ua.gradsoft.managedfixture.FixtureStateTypes] 
                                                               extends org.scalatest.fixture.Suite
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

  protected def fixtureStateManager: FixtureStateManager[T] = fixtureAccess.fixtureStateManager;

  protected def neededFixtureStates: MutableMap[String, TestFixtureStateUsageDescription[T]] = 
                                      if (isNested) {
                                        _parent.get.neededFixtureStates
                                      } else {
                                        _neededFixtureStates
                                      }

  private lazy val _neededFixtureStates: MutableMap[String,TestFixtureStateUsageDescription[T]] =
                                                                                         LinkedHashMap();

  override def nestedSuites = suitesToRun.values.toIndexedSeq;
  protected lazy val suitesToRun: MutableMap[String,Suite] = MutableMap[String,Suite]();

  private[scalatest] lazy val defaultFixtureState = TestFixtureStateUsageDescription[T](fixtureStateTypes);
  private[scalatest] var fixtureStateForNextTest:Option[TestFixtureStateUsageDescription[T]] = None;
  

  def withFixture(test: OneArgTest) =
  {
    val x = neededFixtureStates.get(test.name).getOrElse(
             throw new IllegalStateException("Can't find fixture state for test:"+test.name+",  available keys="+neededFixtureStates.keys)
            )
    fixtureStateManager.doWith(x, test)
  }

  def fixtureUsage(dsl:DSLExpression):Unit = 
    { 
      fixtureStateForNextTest = Some(dsl.value); 
    }

  def fixtureUsage(usage: TestFixtureStateUsageDescription[T]):Unit = 
    { 
      fixtureStateForNextTest = Some(usage); 
    }


  protected def isNested : Boolean = (_parent != None);

  protected var _parent : Option[AbstractManagedFixtureStateSuite[T]] = {
           ManagedFixtureStateSuiteConstructorKluge.currentParent.value map { x =>
                x.asInstanceOf[AbstractManagedFixtureStateSuite[T]];
           }
  }

  protected var _parentTestName : Option[String] = {
           ManagedFixtureStateSuiteConstructorKluge.currentTestName.value map { x =>
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

  override def run(testName: Option[String], args: Args): Status =
  {
    if (testNames.isEmpty) {
      if (!nestedSuites.isEmpty) {
        //TODO: wrap args with new reporter.
       testName match {
          case None => this.runNestedSuites(args)
          case Some(name) =>
              new CompositeStatus(
                nestedSuites.withFilter(
                    _.testNames.contains(name)
                ).map(
                    _.run(testName, args)
                ).toSet
              )
       }
      } else {
        // nothing to test.
        SucceededStatus
      }
    } else {
       super.run(testName, args);
    }
  }


  protected override def runNestedSuites(args:Args): Status =
  {
   if (!isNested) {
    // run subsuites in terms of order
    val sequenceParts = ExecutionSequenceOptimizer.optimizeOrder(neededFixtureStates);
    val optLock = fixtureAccess.suiteLevelLock;
    optLock.foreach( _.acquire() )
    try {
      val status = new StatefulStatus()
      runNestedSuitesParts(sequenceParts, args, status)
      status.waitUntilCompleted()
      status
    } finally {
      optLock.foreach( _.release() )
    }
   } else {
     super.runNestedSuites(args);
   }
  }

  protected def runNestedSuitesParts(parts: List[List[String]], args: Args, status: StatefulStatus): Status =
  {
    parts match {
      case head::tail =>  
                          runNestedSuitesPart(head, args).whenCompleted{ succeeded =>
                                                     if (!succeeded) {
                                                        status.setFailed()
                                                     }
                                                     runNestedSuitesParts(tail,args,status) 
                          }
      case Nil => status.setCompleted()
    }
    status
  }



  protected def runNestedSuitesPart(part: List[String], args: Args): Status =
  {
    val statuses = part map {
      nested => args.distributor match {
                   case Some(d) => d.apply(suitesToRun(nested),args)
                   case None => suitesToRun(nested).run(None,args)
                }
    }
    new CompositeStatus(statuses.toSet)
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
