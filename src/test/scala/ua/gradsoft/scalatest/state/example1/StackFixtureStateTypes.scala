package ua.gradsoft.scalates.state.example1

import org.scalatest._
import ua.gradsoft.managedfixture._

import scala.collection.mutable.Stack;

/**
 * This is formal minimal example, where state
 **/
object StackFixtureStateTypes extends FixtureStateTypes
{

  type FixtureType = Stack[Int];

  object States extends Enumeration
  {
    val empty, nonEmpty = Value;
  }

  val startStates = States;

}


// vim: set ts=4 sw=4 et:
