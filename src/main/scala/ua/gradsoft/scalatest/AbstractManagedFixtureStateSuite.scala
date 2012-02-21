package ua.gradsoft.scalatest

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;

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
trait AbstractManagedFixtureStateSuite[T <: FixtureStateTypes] extends fixture.Suite
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

  protected val fixtureStateManager = new FixtureStateManager[T](fixtureAccess);

  private[scalatest] val neededFixtureStates: MutableMap[String,TestFixtureStateUsageDescription[T]] =
                                                                                         LinkedHashMap();

  private[scalatest] var defaultFixtureState = TestFixtureStateUsageDescription[T](fixtureStateTypes);
  private[scalatest] var fixtureStateForNextTest = defaultFixtureState;
  

  def withFixture(test: OneArgTest) =
  {
    val x = neededFixtureStates.get(test.name).getOrElse(defaultFixtureState);
    fixtureStateManager.doWith(x, test);
  }

  def fixtureUsage(dsl:DSLExpression):Unit = 
    { fixtureStateForNextTest = dsl.value; }


}


// vim: set ts=4 sw=4 et:
