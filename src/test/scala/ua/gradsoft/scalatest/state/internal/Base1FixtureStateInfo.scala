package ua.gradsoft.scalates.state.internal

import org.scalatest._
import ua.gradsoft.scalatest.state._


/**
 * This is formal minimal example, where state
 * represented by int.
 **/
object Base1FixtureStateInfo extends FixtureStateInfo
{

  type FixtureType = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  val startStates = States;

  override def load(s: StartStateType): FixtureType = 
  {
   import States._
   s match {
      case ONE => 1
      case TWO => 2
      case THREE => 3
   }
  }

}


// vim: set ts=4 sw=4 et:
