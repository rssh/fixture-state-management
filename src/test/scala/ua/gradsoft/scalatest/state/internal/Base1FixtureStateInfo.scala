package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.testing._


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

object Base1FixtureStateOperations extends FixtureStateOperations[Base1FixtureStateInfo.type]
{
  import Base1FixtureStateInfo.States._ ;


  override def load(f:Option[FixtureType], 
                    s: StartStateType): FixtureType = 
  {
   //System.err.println("load state:"+s);
   emulatedState = s;
   s match {
      case ONE => 1
      case TWO => 2
      case THREE => 3
   }
  }

  override def close(f:FixtureType): Unit = {} 

  // set by tests. in real life 
  // usually state are outside of program control.
  def set(s: StartStateType):Unit = {
     emulatedState = s;
  }
  private var emulatedState: StartStateType = ONE;

}


// vim: set ts=4 sw=4 et:
