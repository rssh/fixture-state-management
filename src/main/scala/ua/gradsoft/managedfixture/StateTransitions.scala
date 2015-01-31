package ua.gradsoft.managedfixture

class StateIndex(val v:Int) extends AnyVal

class OperationIndex(val v: Int) extends AnyVal

case class PathInfo(ops:Seq[(OperationIndex,StateIndex)],weight:Int)


class StateTransitions[A,Fixture,State](operations:Seq[FixtureAccessOperation[A,Fixture,State]])
{

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

       def outputEdjes(i:StateIndex): Set[(OperationIndex,StateIndex)] =
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


}
