package ua.gradsoft.managedfixture

import scala.concurrent._


case class FixtureAccessOperation[A,Fixture,State](f: Fixture => A, change: FixtureStateChange[State])

/**
 * Let's imagine box, where one instance of fixture is
 * live and all operations are applied sequentially.
 **/
trait FixtureAccessBox[Fixture,State]
{

  def load(s: State): Future[this.type]

  def apply[A](op: FixtureAccessOperation[A,Fixture,State]): Future[(A,this.type)]

  def close(): Future[Unit]

}


