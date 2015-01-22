package ua.gradsoft.scalates.state.example1

import org.scalatest._
import ua.gradsoft.managedfixture._
import scala.concurrent._
import scala.util._
import java.util.concurrent.Executors

import scala.collection.mutable.Stack;



object StackStates extends Enumeration
{
    val Empty, NonEmpty = Value;
}

class StackAccessBox extends FixtureAccessBox[Stack[Int],StackStates.Value]
{

  def load(s:StackStates.Value): Future[this.type] =
  {
    import StackStates._
    val p = Promise[this.type]
    executor.submit(new Runnable(){
        def run():Unit =
        {
          s match {
             case Empty => stack.clear()
             case NonEmpty => stack.push(1) 
          }
          p success StackAccessBox.this
        }
    })
    p.future
  }

  def apply[A](op: FixtureAccessOperation[A,Stack[Int],StackStates.Value]): Future[(A,this.type)] =
  {
    val p = Promise[(A,this.type)]
    executor.submit(new Runnable(){
        def run():Unit =
        {
          p complete Try((op.f(stack),StackAccessBox.this))
        }
    })
    p.future
  }
       
  def close(): Future[Unit] =
         Future successful (())

  private val stack = new Stack[Int]()
  private val executor = Executors.newSingleThreadExecutor() 
}

// vim: set ts=4 sw=4 et:
