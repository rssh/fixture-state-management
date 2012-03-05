/*
 * Copyright 2012 Ruslan Shevchenko
 * Copyright 2012 GradSoft Ltd
 *
 * Default licensing: Apache
 *
 */

package ua.gradsoft.managedfixture

import scala.collection.mutable.{Queue => MutableQueue}
                     
object ExecutionSequenceOptimizer
{


  /**
   * sort states in order, which allow sequental/parallel computations with minimal number of reloads
   * between.
   **/
  def optimizeOrder[T <: FixtureStateTypes](
         usageDescriptions: scala.collection.Map[String,TestFixtureStateUsageDescription[T]]):List[List[String]] =
  {
    var stateTransfer : Map[T#StartStateType, Map[T#StartStateType,List[String]]] = Map();
    var stateSame: Map[T#StartStateType, List[String]] = Map();
    var stateUndefined: Map[T#StartStateType, List[String]] = Map();
    for(x <- usageDescriptions; state <- x._2.precondition.allowedStartStates) {
       x._2.startStateChange match {
          case NewState(f) => 
                      stateTransfer.get(state) match {
                          case Some(xt) => 
                                  xt.get(f) match {
                                    case Some(xtf) => 
                                           stateTransfer=stateTransfer.updated(
                                                            state, xt.updated(f, x._1 :: xtf))
                                    case None =>
                                           stateTransfer=stateTransfer.updated( state , 
                                                                   xt.updated(f, x._1::Nil))
                                  }
                           case None => 
                                 stateTransfer=stateTransfer.updated( state , 
                                               Map[T#StartStateType, List[String]]( f -> List(x._1)) )
                      }
          case SameState => stateSame = stateSame.updated(state, 
                                              x._1 :: stateSame.getOrElse(state,Nil))
          case UndefinedState => stateUndefined = stateUndefined.updated(state, x._1 ::
                                                    stateUndefined.getOrElse(state,Nil))
       }
    } 
    // now in same state sort all items in order of minimal  
    var stateSameSorted = stateSame
    for( (state, testInStates) <- stateSame) {
        val sorted = testInStates.sortWith(
               usageDescriptions(_).stateAspectsChanged.size < 
                           usageDescriptions(_).stateAspectsChanged.size)
        stateSameSorted=stateSameSorted.updated(state,sorted);
    }
    stateSame = stateSameSorted;
    //
    val sBegin = StepState[T](usageDescriptions,
                              stateSame,
                              stateUndefined,
                              stateTransfer,
                              None,
                              Set(),
                              Nil,
                              usageDescriptions.keySet.toSet,
                              0);
    val sEnd = generateAndSelect(sBegin);
    sEnd.buildPath.reverse;
  }

               
  case class StepState[T <: FixtureStateTypes](
        val  usageDescriptions: scala.collection.Map[String,TestFixtureStateUsageDescription[T]],
        val  stateSame:Map[T#StartStateType,List[String]],
        val  stateUndefined:Map[T#StartStateType,List[String]],
        val  stateTransfer : Map[T#StartStateType, Map[T#StartStateType,List[String]]],
        val  currentState: Option[T#StartStateType],
        val  currentUsedAspects: Set[T#StateAspectType],
        // path to build: reversed.
        val  buildPath: List[List[String]],
        val  namesToLeft: Set[String],
        val  currentNLoads: Int )
  {

   def compatibleWith(candidateName: String,
                      mustBeParallel: Boolean): Boolean =
       compatibleWith(currentState.get, currentUsedAspects, mustBeParallel, candidateName); 
  
   def compatibleWith(state:T#StartStateType, 
                      usedAspects:Set[T#StateAspectType],
                      mustBeParallel: Boolean,
                      candidateName: String):Boolean =
   {
     val d = usageDescriptions(candidateName);
     (d.precondition.allowedStartStates.contains(state)) && {
       ((usedAspects intersect d.precondition.neededStateAspects).isEmpty) && {
          if (mustBeParallel) {
            d.canRunParallel
          } else {
            true
          }
       } 
     }
   }


  }


  /**
   * based on state <code> in </code> generate variants for possible next steps, based
   * on simple heuristics.
   **/
  def doStep[T <: FixtureStateTypes](in: StepState[T], nVariants: Int):List[StepState[T]] =
  {
   // - do 
   var retval: List[StepState[T]] = Nil;
   var foundVariants = 0;
   var stopSearch = false;
   in.currentState match {
    case Some(currentState) =>
     in.stateSame get currentState match {
       case Some(l) =>
           var parallel = l.filter(in.compatibleWith(_,true));
           if (!parallel.isEmpty) {
             val namesToLeft = (in.namesToLeft -- parallel);
             retval=in.copy[T](
                       stateSame=in.stateSame.mapValues(_.filter(namesToLeft.contains(_)) ),
                       stateUndefined=in.stateUndefined.mapValues(_.filter(namesToLeft.contains(_)) ),
                       stateTransfer=in.stateTransfer.mapValues(_.mapValues( 
                                                                  _.filter(namesToLeft.contains(_)) )),
                       buildPath=parallel::in.buildPath,
                       namesToLeft = namesToLeft
                    )::retval;
             foundVariants = foundVariants + 1;
             stopSearch=true;
           } else {
             val sequential = l.filter(in.compatibleWith(_,false)).headOption;
             for(s <- sequential) {
                 val d = in.usageDescriptions(s);
                 val namesToLeft = (in.namesToLeft - s);
                 retval=in.copy[T](
                       stateSame=in.stateSame.mapValues(_.filter(namesToLeft.contains(_)) ),
                       stateUndefined=in.stateUndefined.mapValues(_.filter(namesToLeft.contains(_)) ),
                       stateTransfer=in.stateTransfer.mapValues(_.mapValues( 
                                                                  _.filter(namesToLeft.contains(_)) )),
                       buildPath=List(s)::in.buildPath,
                       namesToLeft = namesToLeft,
                       currentUsedAspects = (in.currentUsedAspects union d.stateAspectsChanged)
                        )::retval;
                 foundVariants = foundVariants + 1;
                 stopSearch=true;
             }
           }
       case _ => /* nothing */
     }
     if (! stopSearch && foundVariants < nVariants) {
      //i.e. we not able get same state, so let's try to do transfers to other state
      in.stateTransfer.get(currentState) match {
         case Some(l) =>
             val variants = l.mapValues( _.filter(in.compatibleWith(_,false))
                                       ).filterNot( _._2.isEmpty ).take(nVariants);
             for((n,s0) <- variants; s <- s0) {
               val namesToLeft = (in.namesToLeft - s);
               retval=in.copy[T](
                    currentState = Some(n),
                    currentUsedAspects = Set[T#StateAspectType](),
                    buildPath = List(s)::in.buildPath,
                    namesToLeft = namesToLeft,  
                    stateSame=in.stateSame.mapValues(_.filter(namesToLeft.contains(_)) ),
                    stateUndefined=in.stateUndefined.mapValues(_.filter(namesToLeft.contains(_)) ),
                    stateTransfer=in.stateTransfer.mapValues(_.mapValues( 
                                                              _.filter(namesToLeft.contains(_)) ))
               )::retval;
              foundVariants = foundVariants + 1;
             }
             retval = retval.take(nVariants);
         case _ => /* nothing */
      }
     }
     if (foundVariants < nVariants) {
      in.stateUndefined.get(currentState) match {
         case Some(l) =>
            val variants = l.filter(in.compatibleWith(_,false)).take(nVariants);
            for(s <- variants) {
               val namesToLeft = (in.namesToLeft - s);
               retval=in.copy[T](
                    currentState = None,
                    currentUsedAspects = Set(),
                    buildPath = List(s)::in.buildPath,
                    namesToLeft = namesToLeft, 
                    stateSame=in.stateSame.mapValues(_.filter(namesToLeft.contains(_)) ),
                    stateUndefined=in.stateUndefined.mapValues(_.filter(namesToLeft.contains(_)) ),
                    stateTransfer=in.stateTransfer.mapValues(_.mapValues( 
                                                              _.filter(namesToLeft.contains(_)) ))
               )::retval;
               foundVariants = foundVariants + 1;
            }
         case _ => /* so, this state is exhoused. */
      }
     }
    case None => /* have no state, so must to load one */
   }
   if (foundVariants == 0) {
     // so, need to load new state anyway
     for(s <- in.namesToLeft.take(nVariants)) {
        // for now - just load this state. 
        val d = in.usageDescriptions(s);
        val state=d.precondition.stateToLoad;
        val namesToLeft = (in.namesToLeft - s);
        retval=in.copy[T](
                 currentState=Some(state),
                 currentUsedAspects=d.stateAspectsChanged,
                 buildPath = List(s)::in.buildPath,
                 namesToLeft = namesToLeft,
                 currentNLoads = in.currentNLoads + 1,
                 stateSame=in.stateSame.mapValues(_.filter(namesToLeft.contains(_)) ),
                 stateUndefined=in.stateUndefined.mapValues(_.filter(namesToLeft.contains(_)) ),
                 stateTransfer=in.stateTransfer.mapValues(_.mapValues( 
                                                              _.filter(namesToLeft.contains(_)) ))
               )::retval;
        foundVariants = foundVariants + 1;
     }
   }
   retval;
  }
  
  def generateAndSelect[T <: FixtureStateTypes](iniStep:StepState[T]):StepState[T] =
  {
    var fifo = MutableQueue[StepState[T]]();
    val maxFifoSize = 10;
    val selectFifoSize = 4;
    val nVariants = 3;
    var endStates = MutableQueue[StepState[T]]();
    var quit = false;
    fifo.enqueue(iniStep);
    val nTests = iniStep.usageDescriptions.size;
    while(fifo.size > 0) {
       val s = fifo.dequeue;
       if (s.namesToLeft.isEmpty) {
         endStates.enqueue(s);
       } else {
         fifo.enqueue(doStep(s, nVariants).toSeq: _*);
       }
       if (fifo.size > maxFifoSize) {
         fifo = fifo.sortWith( (a,b) =>
           // (S - size(leftA))    (S - size(leftB))
           // ----------------- >  -----------------  =  
           //      nLoadsA           nLoadsB
           //
           // (S - size(leftA))*nLoadsB > (S-size(leftB))*nLoadsA =
           //
           (nTests - a.namesToLeft.size)*b.currentNLoads > 
                (nTests - b.namesToLeft.size)*a.currentNLoads
         ).take(selectFifoSize).toQueue;
       }
    }
    // and finally, let's get one with minimal nLoads.
    endStates.sortWith(_.currentNLoads < _.currentNLoads).head;
  }

}


// vim: set ts=4 sw=4 et:
