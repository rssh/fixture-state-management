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

class StackAccessBox extends FixtureAccessBox[Stack[Int]]
{

  def apply[A](f: Stack[Int] => A): Future[(A,this.type)] =
  {
    val p = Promise[(A,this.type)]
    executor.submit(new Runnable(){
        def run():Unit =
        {
          p complete Try((f(stack),StackAccessBox.this))
        }
    })
    p.future
  }
       
  def close() = {}

  private val stack = new Stack[Int]()
  private val executor = Executors.newSingleThreadExecutor() 
}

// vim: set ts=4 sw=4 et:
