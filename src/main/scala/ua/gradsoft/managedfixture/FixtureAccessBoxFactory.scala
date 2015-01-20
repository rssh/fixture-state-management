package ua.gradsoft.managedfixture

import scala.concurrent._

/**
 * Test authors must implement this trait for wrapping access to managed fixture.
 */
trait FixtureAccessProvider[F,S]
{

  type Fixture = F

  type State = S

  /**
   * create or get instance of FixtureAccessBox in initial state.
   **/
  def box(): Future[FixtureAccessBox[Fixture,State]]

  def allStates:Set[State] 

  def initialState: Option[State]

}


// vim: set ts=4 sw=4 et:
