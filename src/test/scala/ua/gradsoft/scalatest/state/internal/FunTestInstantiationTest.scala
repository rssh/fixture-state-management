package ua.gradsoft.scalatest.state.internal


import scala.concurrent._
import org.scalatest._
import ua.gradsoft.managedfixture._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ConcurrentLinkedQueue

class MyFunGroupSuite extends managedfixture.GroupSuite[AtomicInteger,Int]
{

   class AtomicIntegerBox(factory: AtomicIntegerBoxFactory) extends FixtureAccessBox[AtomicInteger]
   {
       
       def apply[A](f: AtomicInteger=>A ): Future[A] = {
          Future successful f(v) 
        }

       def close() = {
         factory.boxReturn()
       }

       val v = new AtomicInteger

   }

   class AtomicIntegerBoxFactory extends FixtureAccessBoxFactory[AtomicInteger]
   {

       def box() =
       {
         val p = Promise[AtomicIntegerBox]()
         val br = boxRef.getAndSet(null)
         if (br!=null) {
            p success br
         } else {
            waitQueue.offer(p)
            val br = boxRef.getAndSet(null)
            if (br != null) {
              p success br
              waitQueue.remove(p)
            }
         }
         p.future
       }

       def boxReturn() =
       {
         Option(waitQueue.poll()) match {
            case Some(x) => {
                    x success _box 
                 }
            case None => 
                 boxRef.set(_box)
         }
       }

       def nBoxes = Some(1)

       private[this] val waitQueue = new ConcurrentLinkedQueue[Promise[AtomicIntegerBox]]()
       private[this] val _box = new AtomicIntegerBox(this)
       private[this] val boxRef = new AtomicReference(_box)
   }

   val fixtureAccessBoxFactory = new AtomicIntegerBoxFactory()

}

class MyFunTest(g: managedfixture.GroupSuite[AtomicInteger,Int],
                f: Option[AtomicInteger],
                testToRun:Option[String]) 
                   extends managedfixture.FunSuite[AtomicInteger,Int](g,f,testToRun)
{

  start state(2) change nothing
  test("TEST-2") { x =>
     assert(x.get()==2)
  }

  start state(3) change nothing
  test("TEST-3") { x =>
     assert(x.get()==3)
  }
  
  start state(any) finish state(2)
  test("TEST:any->2") { x =>
     x.set(2)
  }

  start state(any) finish state(3)
  test("TEST:any->3") { x =>
     x.set(3)
  }

}

class FunTestInstantiationTest extends FunSuite
{

  test("fixture FunTest must be able to instantiate yourself") {
    val g = new MyFunGroupSuite
    val t0 = new MyFunTest(g,None,None)
    val t1_2 = t0.createCopy(g,Some(new AtomicInteger(2)),Some("TEST-2"))
    assert(t1_2.isInstanceOf[MyFunTest])
    val t1_3 = t0.createCopy(g,Some(new AtomicInteger(3)),Some("TEST-3"))
    //t1_2.run
  }

  test("ff") {
    pending
  }


}

// vim: set ts=4 sw=4 et:
