package ua.gradsoft.managedfixture

import scala.concurrent._


case class FixtureAccessOperation[A,Fixture,State](
                        val f: Fixture => A, 
                        val usage: FixtureStateUsageDescription[State]
                        ) extends IndexedByFixtureUsage[Fixture=>A,State]
{
  override def value = f
}

/**
 * Let's imagine box, where instance of fixture is
 * live and all operations are applied sequentially.
 **/
trait FixtureAccessBox[Fixture,State]
{

  def load(s: State): Future[this.type]

  def apply[A](op: FixtureAccessOperation[A,Fixture,State]): Future[(A,this.type)]

  /**
   * close (if necessory) fixture access box, after all currently evaluated
   * operations will be finished.
   **/
  def close(): Future[Unit]

}


