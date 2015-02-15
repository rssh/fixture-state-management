package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._
import java.util.concurrent.atomic.AtomicInteger

class StateTransitionsTest extends FunSuite
                             with FixtureStateDSL[Int]
{

   def | = new FixtureStateVerb

   test("build linear incidence matrix") {

     val u1 =  | start state(1) finish state(2)
     val u2 =  | start state(2) finish state(3)
     val u3 =  | start state(3) finish state(1)
               

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

   test("build incidence matrix with any->any state") {
     val u0 =  | start state(any) finish state(1)
     val u1 =  | start state(1) finish state(2)
     val u2 =  | start state(2) finish state(3)
     val u3 =  | start state(3) finish state(1)
     val u4 =  | start state(any) change nothing

     val f0 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                        _.set(1), u0.value)
     val f1 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                        _.compareAndSet(1,2), u1.value)
     val f2 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                        _.incrementAndGet, u2.value)
     val f3 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                               _.compareAndSet(3,1), u3.value)
     val f4 = FixtureAccessOperation[Unit,AtomicInteger,Int](
                                               _.get, u4.value)

     val stateTransitions = new StateTransitions(Seq(f0,f1,f2,f3,f4))
     val im = stateTransitions.incidenceMatrix
     assert(im.get(0,0).size==1)
     assert(im.get(0,1).size==1)
     assert(im.get(0,2).size==0)
     assert(im.get(0,3).size==0)
     assert(im.get(1,0).size==0)
     assert(im.get(1,1).size==1)
     assert(im.get(1,2).size==1)
     assert(im.get(1,3).size==0)
     assert(im.get(3,0).size==1) 
     val pathes = stateTransitions.initialPathes
     val p = pathes.get(2) // state 3, i.e. 
     assert(p.isDefined) 
     assert(p.get.weight==3)  // Ini->1, 1->2, 2->3,
   }

}
