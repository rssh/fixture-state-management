package ua.gradsoft.managedfixture

import scala.concurrent._


/**
 * Let's imagine box, where instance of fixture is
 * live and all operations are applied sequentially.
 **/
trait FixtureAccessBox[Fixture]
{

  /**
   * apply operation on fixture
   **/
  def apply[A](f: Fixture => A): Future[A]

  /**
   * close (if necessory) fixture access box, after all currently evaluated
   * operations will be finished.
   **/
  def close(): Unit

}


