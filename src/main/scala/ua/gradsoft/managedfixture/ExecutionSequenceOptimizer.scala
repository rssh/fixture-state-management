package ua.gradsoft.managedfixture

object ExecutionSequenceOptimizer {

   /**
    * return sequence of sequence-of-operaions , which in summary
    * cover all operations in state-transitions. Each sequence is
    * started from initial state.
    *
    *@param st state transitions
    *@param n approx number of segments. Note, that we can return more number of segments, than
    * requested.
    *@return set of segments, each segments is sequence of operation indexes in
    * st.operations
    **/
   def apply[A,S](st:StateTransitions[A,S], n: Int):IndexedSeq[IndexedSeq[Int]] =
   {
    val v = StateVariant(st, IndexedSeq(), IndexedSeq(), st.initialStateIndex, 
                         Set(), ((0 until st.operations.length) map (new OperationIndex(_))).toSet, 0 )
    var next = IndexedSeq(v)
    var finished = IndexedSeq()
    var found = false 
    var retval: IndexedSeq[IndexedSeq[Int]] = IndexedSeq()
    while(!found) {
       var candidates = for( c <- next;
                             x <- genNVariants(c,n)) yield x
       val (finished, nonFinished) = candidates.partition(_.rest.isEmpty)
       if (! finished.isEmpty) {
           retval = finished.sortBy(_.weight).head.buildPart map (_ map (_.v))
           found=true
       } else {
           next = nonFinished.sortBy(_.weight).take(n) 
       }
    }
    retval 
  }

   
   case class StateVariant[A,S](
          st: StateTransitions[A,S],
          buildPart: IndexedSeq[IndexedSeq[OperationIndex]],
          current: IndexedSeq[OperationIndex],
          lastState:  StateIndex,
          used:    Set[OperationIndex],
          rest:    Set[OperationIndex],
          weight:  Int
   )

   def  genNVariants[A,S](v:StateVariant[A,S],nVariants:Int):IndexedSeq[StateVariant[A,S]] =
   {
      var nextCandidates=v.st.incidenceMatrix.outEdjes(v.lastState)
      var retval: IndexedSeq[StateVariant[A,S]] = IndexedSeq()
      while(!nextCandidates.isEmpty && retval.size < nVariants) {
          val c = nextCandidates.head
          nextCandidates = nextCandidates.tail
          if (v.rest contains c._1) {
             val nv = v.copy(
                          current = v.current :+ c._1,
                          lastState = c._2,
                          used = v.used + c._1,
                          rest = v.rest - c._1
                      )
             retval = retval :+ nv
          }
      }

      var crest = v.rest
      while (retval.size < nVariants && !crest.isEmpty) {
         val c = crest.head 
         crest  = crest.tail
         val ns: Set[StateIndex] =  v.st.operation(c).usage.precondition match {
            case AnyState =>
                   throw new IllegalStateException(
                     s"""Invalid stateTransitions: AnyState rest must be in out edjes 
                          c=${c}  lastState=${c}
                      """
                   )
             case States(states) => states map (v.st.stateIndexes.byState(_))
         }
         for(s <- ns) {
             val pi = v.st.initialPathes.get(s).getOrElse{
                        throw new IllegalStateException(s"state ${v.st.printState(s)} is unreachable")
                      }
             val iniOps = pi.ops.map(_._1)
             val iniOpsSet = iniOps.toSet
             val newRest = (v.rest - c) &~ iniOpsSet
             val newAdded = (v.rest - c) & iniOpsSet
             val newUsed = (v.used + c) ++ newAdded
             val nv = v.copy(
                         buildPart = v.buildPart :+ v.current,
                         current = iniOps.toIndexedSeq :+ c,
                         lastState = v.st.applyChange(v.lastState,v.st.operation(c).usage.startStateChange),
                         rest = newRest,
                         used = newUsed,
                         weight = v.weight + iniOps.size - newAdded.size
                      )
             retval = retval :+ nv
         }
      }

      retval = retval.sortBy(_.weight)
      retval = retval.take(nVariants)

      retval
   }


}
