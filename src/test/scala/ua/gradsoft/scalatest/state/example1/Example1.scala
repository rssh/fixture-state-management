package ua.gradsoft.scalates.state.example1

import org.scalatest._
import ua.gradsoft.managedfixture._


/**
 * This is formal minimal example, where state
 * represented by int.
 **/
class Example1FixtureStateTypes extends FixtureStateTypes
{

  type Fixture = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  type State = States.Value

  val allStates = States;

}

object Example1FixtureStateTypes extends Example1FixtureStateTypes;




// vim: set ts=4 sw=4 et:
