package ua.gradsoft.scalates.state.example1

import org.scalatest._
import ua.gradsoft.scalatest.state._


/**
 * This is formal minimal example, where state
 * represented by int.
 **/
class Example1FixtureStateTypes extends FixtureStateTypes
{

  type FixtureType = Int;

  object States extends Enumeration
  {
    val ONE, TWO, THREE = Value;
  }

  val startStates = States;

}

object Example1FixtureStateTypes extends Example1FixtureStateTypes;

//class Example1TestSuites extends StatefullFixture.Suite(Examples1StatesInfo)
class Example1TestSuites extends Suite
{

  /**
   *test that load state and do nothing.
   **/
/*
  state test("check-one") in(ONE) change nothing in {
    val f = fixture();
    f should be 1
  }
*/

//  state test("check-any") in(ANY) change nothing in {

  /**
   * test that change state t
   **/
/*
  state test("one -> two") in(ONE) out(TWO) change nothing in {
    val f = fixture();
    f should be 1
    
  }
*/


}



// vim: set ts=4 sw=4 et:
