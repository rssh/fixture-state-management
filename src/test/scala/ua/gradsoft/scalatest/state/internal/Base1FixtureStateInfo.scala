package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._

import scala.concurrent._

/**
 * This is formal minimal example, where state
 * represented by int.
 **/
object Base1FixtureStateInfo extends FixtureStateTypes
{

  type Fixture = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  val allStates = States;

}

class Base1FixtureAccessBox extends FixtureAccessBox[Base1FixtureStateInfo.type]
{

   def load(s: Base1FixtureStateInfo.State): Future[Base1FixtureAccessBox] =
   {
     value = s match {
       case ONE => 1
       case TWO => 2
       case THREE => 3
     }
   }   

   def apply[A](op: FixtureAccessOperation[Base1FixtureStateInfo.State,A]): Future[(A,this.type)] =
     (Future fromTry Try(op.f(value))) map ((_,this))

   var value: Int
}

class Base1FixtureAccessBoxFactory extends FixtureAccessBoxFactory[Base1FixtureStateInfo.type]
{

   override def get() = new Base1FixtureAccessBox

}

// vim: set ts=4 sw=4 et:
