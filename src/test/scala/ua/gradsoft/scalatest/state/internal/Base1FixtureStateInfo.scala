package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._


/**
 * This is formal minimal example, where state
 * represented by int.
 **/
object Base1FixtureStateInfo extends FixtureStateTypes
{

  type FixtureType = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  val startStates = States;

}

object Base1FixtureAccess extends FixtureAccess[Base1FixtureStateInfo.type]
{
  import Base1FixtureStateInfo.States._ ;


  override def load(s: StartStateType): Unit = 
  {
   //System.err.println("load state:"+s);
   emulatedState = s;
  }

  override def acquire(): Option[FixtureType] =
  {
   Some(emulatedState match {
      case ONE => 1
      case TWO => 2
      case THREE => 3
   })
  }

  // set by tests. in real life 
  // usually state are outside of program control.
  def set(s: StartStateType):Unit = {
     emulatedState = s;
  }

  @volatile
  private var emulatedState: StartStateType = ONE;

}


// vim: set ts=4 sw=4 et:
