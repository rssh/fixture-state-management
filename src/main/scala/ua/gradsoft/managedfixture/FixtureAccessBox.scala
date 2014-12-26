package ua.gradsoft.managedfixture

import scala.concurrent._

/**
 * Let's imagine box, where one instance of fixture is
 * live and all operations are applied sequentially.
 **/
trait FixtureAccessBox[T <: FixtureStateTypes]
{

  def load(s: T#State): Future[FixtureAccessBox[T]]

  def apply[A](op: FixtureAccessOperation[T,A]): Future[(A,FixtureAccessBox[T])]

}


