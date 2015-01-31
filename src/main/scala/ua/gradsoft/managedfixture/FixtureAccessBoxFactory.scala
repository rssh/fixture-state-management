package ua.gradsoft.managedfixture

import scala.concurrent._

/**
 * Test authors must implement this trait for wrapping access to managed fixture.
 */
trait FixtureAccessProvider[Fixture,State]
{

  /**
   * create or get instance of FixtureAccessBox in initial state.
   **/
  def box(): Future[FixtureAccessBox[Fixture,State]]

  /**
   * number of boxes, which can be created in parallel.
   * None means, that boxes can be created on demand.
   */
  def nBoxed: Option[Int]

  /**
   * release all resources (i.e. shuodown external tools, etc)
   * needed for boxes.  Will be called after all work in boxes will be finished.
   */
  def close(): Future[Unit]

}


// vim: set ts=4 sw=4 et:
