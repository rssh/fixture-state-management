package ua.gradsoft.managedfixture

import scala.concurrent.Lock;

/**
 * Test authors must implement this trait for wrapping access to managed fixture.
 */
trait FixtureAccessBoxFactory[T <: FixtureStateTypes]
{

  /**
   * how to load given state: i.e. load database dump, initialize vars according, etc..
   * after call of load, current must return fixture with access to given state
   **/
  def create(): FixtureAccessBox[T]


}


// vim: set ts=4 sw=4 et:
