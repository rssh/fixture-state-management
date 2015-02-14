package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._
import java.util.concurrent.atomic.AtomicInteger

class StateTransitionsTest extends FunSuite
                             with FixtureStateDSL[Int]
{

   test("build linear incidence matrix") {

     def | = new FixtureStateVerb

     val u1 =  | start state(1) finish state(2)
     val u2 =  |  start state(2) finish state(3)
     val u3 =  |  start state(3) finish state(1)
               

     val f1 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                        _.compareAndSet(1,2), u1.value)
     val f2 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                               _.incrementAndGet, u2.value)
     val f3 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                               _.compareAndSet(3,1), u3.value)
     val stateTransitions = new StateTransitions(Seq(f1,f2,f3))
     val im = stateTransitions.incidenceMatrix
     assert(im.get(0,0).isEmpty)
     assert(im.get(0,1).nonEmpty)
     assert(im.get(0,2).isEmpty)
     assert(im.get(1,0).isEmpty)
     assert(im.get(1,1).isEmpty)
     assert(im.get(1,2).nonEmpty)
     assert(im.get(2,0).nonEmpty)
     assert(im.get(2,1).isEmpty)
     assert(im.get(2,1).isEmpty)
   }

}
