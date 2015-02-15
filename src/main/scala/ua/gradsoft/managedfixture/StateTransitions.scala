package ua.gradsoft.managedfixture

import scala.collection.mutable.PriorityQueue

class StateIndex(val v:Int) extends AnyVal

object StateIndex
{
  implicit def toStateIndex(v:Int):StateIndex =
    new StateIndex(v)
}

class OperationIndex(val v: Int) extends AnyVal

object OperationIndex
{
  implicit def toOperationIndex(v:Int):OperationIndex =
    new OperationIndex(v)
}


class StateTransitions[A,Fixture,State](operations:Seq[FixtureAccessOperation[A,Fixture,State]])
{

   case class PathInfo(ops:Seq[(OperationIndex,StateIndex)],weight:Int)
   {
     def + (x:(OperationIndex,StateIndex)): PathInfo =
       PathInfo(ops :+ x, weight + 1)
   }

   case class StateIndexes(byState:Map[State,StateIndex],byIndex:IndexedSeq[State])
   {
      def nStates = byIndex.length

      def withState(s:State): StateIndexes =
         byState.get(s) match {
            case Some(x) => this
            case None => StateIndexes(byState + (s-> new StateIndex(nStates)) ,byIndex :+ s)
         }

   }

   val stateIndexes = operations.foldLeft(StateIndexes(Map(),IndexedSeq())){ (s,e) =>
                         val s1 = e.usage.precondition match {
                                     case States(states) => states.foldLeft(s) ( 
                                                               _ withState _
                                                            )
                                     case _ => s
                                  }
                         val s2 = e.usage.startStateChange match {
                                     case NewState(s) => s1 withState s
                                     case _  => s1
                                  }
                         s2
                      }

   val initialStateIndex = new StateIndex(stateIndexes.nStates)
   val terminationStateIndex = new StateIndex(stateIndexes.nStates+1)

   def stateIndex(s:State) = stateIndexes.byState(s)

   lazy val incidenceMatrix = buildIncidenceMatrix() 

   class IncidenceMatrix
   {

       /**
        * return operations which starts in state <code> i </code>
        * and ends in state <code> j </code>
        **/
       def get(i:StateIndex,j:StateIndex): Set[OperationIndex] =
       {
         val r = statePathes.getOrElse(i,Map()).getOrElse(j,Set())
         r ++ (if (i==j) anyUnchanged else Set())
       }

       def outEdjes(i:StateIndex): Set[(OperationIndex,StateIndex)] =
       {
         (for((si,ops) <- statePathes.getOrElse(i,Map()) ;
              op <- ops ) yield (op,si)).toSet ++
                              (anyUnchanged map((_,i)) ) 
       }
         
       def addOperation(operationIndex: OperationIndex, op:FixtureAccessOperation[A,Fixture,State]) =
                op.usage.precondition match {
                   case States(states) =>
                           for(state <- states) {
                               val si = stateIndex(state)
                               addOperationForState(si,operationIndex,op)
                           }
                   case AnyState =>
                           addOperationForAnyState(operationIndex,op)
                }

       def addOperationForAnyState(operationIndex:OperationIndex, op:FixtureAccessOperation[A,Fixture,State]) =
             op.usage.startStateChange match {
                 case SameState => anyUnchanged += operationIndex
                 case _ => addOperationForState(initialStateIndex, operationIndex, op)
              }

       def addOperationForState(si: StateIndex, oi:OperationIndex, op:FixtureAccessOperation[A,Fixture,State]) = 
       {
             op.usage.startStateChange match {
                 case SameState =>
                            addOperationForStates(si,si,oi,op)
                 case NewState(r) =>
                            addOperationForStates(si,stateIndex(r),oi,op)
                 case UndefinedState =>
                            addOperationForStates(si,terminationStateIndex,oi,op)
             }
       }

          
       def addOperationForStates(s1: StateIndex, s2:StateIndex, operationIndex:OperationIndex, op:FixtureAccessOperation[A,Fixture,State]) = 
       {
         val inner1 = statePathes.getOrElse(s1,Map())
         val inner2 = inner1.getOrElse(s2,Set())
         val inner2u = inner2 + operationIndex
         val inner1u = inner1 + (s2->inner2u)
         statePathes= statePathes + (s1->inner1u)
       }

       var statePathes: Map[StateIndex,Map[StateIndex,Set[OperationIndex]]] = Map()
       var anyUnchanged: Set[OperationIndex] = Set()
   }

   def buildIncidenceMatrix():IncidenceMatrix =
   {
    val im = new IncidenceMatrix()
    for((idx,op) <- operations.zipWithIndex) im.addOperation(op,idx)
    im
   }

   lazy val initialPathes = new InitialPathes(incidenceMatrix)

   /**
    * implementation of Deikstra algorithm, 
    * here we hold pathes from initial to all node except termination
    **/
   class InitialPathes(m:IncidenceMatrix)
   {

     def build()
     {
      var queue = new PriorityQueue[(StateIndex,PathInfo)]()(new Ordering[(StateIndex,PathInfo)]{
                                                             override def compare(x:(StateIndex,PathInfo),y:(StateIndex,PathInfo)):Int =
                                                                       x._2.weight - y._2.weight
                                                            });
      val emptyPath = PathInfo(Seq(),0)
      put(initialStateIndex, emptyPath)
      queue += ((initialStateIndex,emptyPath))
      while(!queue.isEmpty) {
         val (si,pi) = queue.dequeue
         processNode(si,pi,queue)
      }
     } 

     def processNode(si:StateIndex,pathInfo: PathInfo, queue: PriorityQueue[(StateIndex,PathInfo)]): Unit =
     {
       for((oi,nsi) <- m.outEdjes(si) if (nsi!=si && nsi!=terminationStateIndex) ) {
          val newPathInfo = pathInfo + ((oi,nsi))
          get(nsi) match {
             case Some(xPathInfo) => if (newPathInfo.weight < xPathInfo.weight) {
                                        put(nsi,newPathInfo)
                                     } 
             case None => put(nsi,newPathInfo)
                          queue.enqueue((nsi,newPathInfo));
          }
       }
     }

     def get(si:StateIndex):Option[PathInfo] = pathes.get(si)

     def nReachableStates = pathes.size

     def nUnreachableStates = stateIndexes.nStates + 1 - nReachableStates ;

     def unreachableStates(): Set[State] =
       stateIndexes.byState.foldLeft(Set[State]()){ (s,e) =>
          if (pathes.contains(e._2)) {
              s
          } else {
              s + e._1
          }
       }

     private def put(si: StateIndex, pi: PathInfo): Unit =
         pathes = pathes.updated(si,pi)
     
     var pathes: Map[StateIndex,PathInfo] = Map()

     build()
   }


}
