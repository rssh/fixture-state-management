package ua.gradsoft.scalates.state.internal

import org.scalatest._
import ua.gradsoft.scalatest.state._


/**
 * This is formal minimal example, where state
 * represented by int.
 **/
class Base1FixtureStateInfo extends FixtureStateInfo[Base1FixtureStateInfo]
{

  type FixtureType = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  val startStates = States;

  override def load(f:Option[Base1FixtureStateInfo#FixtureType], 
                    s: Base1FixtureStateInfo#StartStateType): Base1FixtureStateInfo#FixtureType = 
  {
   System.err.println("load state:"+s);
   import States._
   s match {
      case ONE => 1
      case TWO => 2
      case THREE => 3
   }
  }

  override def close(f:FixtureType): Unit = {} 

}

object Base1FixtureStateInfo extends Base1FixtureStateInfo;

// vim: set ts=4 sw=4 et:
