package ua.gradsoft.managedfixture


import scala.concurrent._
import ua.gradsoft.managedfixture._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ConcurrentLinkedQueue

trait RefBorrowQueue[T<:AnyRef]
{

   def borrowT(): Future[T] =
   {
     val r = ref.getAndSet(null.asInstanceOf[T])
     if (r!=null) {
        Future successful r
     } else {
        val p = Promise[T]()
        waitQueue offer p
        val r1 = ref.getAndSet(null.asInstanceOf[T]) 
        if (r1!=null) {
          waitQueue remove p
          p success r1
        }
        p.future
     }
   }

   def returnT(t:T): Unit =
     Option(waitQueue.poll()) match {
       case Some(q) => q success t
       case None => ref set t 
     }

   val ref = new AtomicReference[T]
   val waitQueue = new ConcurrentLinkedQueue[Promise[T]]()

}

class OneInstanceFixtureAccessBox[T <: AnyRef](x: T, factory: OneInstanceFixtureAccessBoxFactory[T]) extends 
                                                                                 FixtureAccessBox[T] with
                                                                                 RefBorrowQueue[T]
{
  
  def apply[A](f: T=>A ): Future[A] = {
     borrowT() map {x => 
                    try{  
                      f(x)
                    } finally {
                      returnT(x)
                   }}
  }

  def close() = {
      factory.boxReturn(this)
  }

  ref.set(x)

}

case class OneInstanceFixtureAccessBoxFactory[T<:AnyRef](t:T) extends FixtureAccessBoxFactory[T]
                                                          with RefBorrowQueue[OneInstanceFixtureAccessBox[T]]
{

       def box() = borrowT()
             
       def boxReturn(x: OneInstanceFixtureAccessBox[T]) = returnT(x)

       def nBoxes = Some(1)

       ref.set(new OneInstanceFixtureAccessBox(t, this))
}

