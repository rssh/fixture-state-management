package ua.gradsoft.managedfixture

import scala.concurrent._

/**
 * Test authors must implement this trait for wrapping access to managed fixture.
 */
trait FixtureAccessBoxFactory[Fixture]
{

  /**
   * create or get instance of FixtureAccessBox in initial state.
   **/
  def box(): Future[FixtureAccessBox[Fixture]]

  /**
   * number of boxes, which can be created in parallel.
   * None means, that boxes can be created on demand.
   */
  def nBoxes: Option[Int]

  /**
   * release all resources (i.e. shuodown external tools, etc)
   * needed for boxes.  Will be called after all work in all boxes will be finished.
   */
  def shutdown(): Unit = {}

  /**
   * if set to true, than caller contract is to call 'box()' no more then 'nBoxes' times,
   * otherwise - box() canbe called any times and will return future to FixtureAccessBox
   *  which become available after freeing of one of previously buzy instances.
   **/ 
  def requirePreallocation(): Boolean = true

}


// vim: set ts=4 sw=4 et:
