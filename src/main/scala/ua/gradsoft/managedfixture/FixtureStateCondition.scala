package ua.gradsoft.managedfixture



/**
 * StateCondition - what states is needed and what aspects must be cleaned.
 **/
sealed abstract class FixtureStateCondition[T <: FixtureStateTypes](val stateInfo: T)
{

  
  /**
   * check when state satisficy position
   **/
  def check(s:T#State):Boolean = 
                                 allowedStartStates.contains(s);

  /**
   * set of states, from which test can be runned.
   **/
  def allowedStartStates:Set[T#State] 

  /**
   * set of state aspects, which must be clean
   **/
  def neededStateAspects: Set[T#Aspect] 

  /**
   * Choose one state from correct, which is loaded by engine
   * to start this preconditions.
   **/
  def stateToLoad: T#State

  /**
   *  return condition wiht added startState s.
   **/
  def withStartState(s: T#State): FixtureStateCondition[T];


  /**
   * return condition where input state can be any (and used aspects same
   * as current)
   **/
  def withAnyState: FixtureStateCondition[T];

  /**
   * return condition where input state is undefined.
   **/
  def withUndefinedState: FixtureStateCondition[T];

  /**
   * return condition whith input states from s.
   **/
  def withStartStates(s: Seq[T#State]): FixtureStateCondition[T];

  /**
   * return condition whith marked aspects from s
   **/
  def withStateAspects(s: Seq[T#Aspect]): FixtureStateCondition[T];

  def and (other: FixtureStateCondition[T]): FixtureStateCondition[T];
  def or  (other: FixtureStateCondition[T]): FixtureStateCondition[T];

}


case class AnyState[T <: FixtureStateTypes](override val stateInfo: T)
                                             extends FixtureStateCondition[T](stateInfo)
{

  def allowedStartStates: Set[T#State] =
         stateInfo.allStates.asInstanceOf[Set[T#State]]  
  
  def neededStateAspects:Set[T#Aspect] = Set();

  def stateToLoad: T#State = stateInfo.allStates.head;

  def withStartState(s: T#State): FixtureStateCondition[T] = this;

  def withStartStates(s: Seq[T#State]): FixtureStateCondition[T] = this;

  def withAnyState: FixtureStateCondition[T] = this;

  def withUndefinedState: FixtureStateCondition[T] = NoState[T](stateInfo);

  def withStateAspects(s: Seq[T#Aspect]): FixtureStateCondition[T] =
      SetOfStatesAndAspects[T](stateInfo, 
                                allowedStartStates,
                                s.toSet)

  def and(other: FixtureStateCondition[T]) = other;
  def or(other: FixtureStateCondition[T]) = this;

}

case class NoState[T <: FixtureStateTypes](override val stateInfo:T) 
                                             extends FixtureStateCondition(stateInfo)
{
  def allowedStartStates: Set[T#State] = Set();
  def neededStateAspects: Set[T#Aspect] = 
                     stateInfo.allAspects.asInstanceOf[Set[T#Aspect]]

  def stateToLoad: T#State = 
         throw new IllegalStateException("NoState have no state to load");

  def withStartState(s: T#State): FixtureStateCondition[T] = 
      SetOfStatesAndAspects[T](stateInfo, Set(s), neededStateAspects);

  def withStartStates(s: Seq[T#State]): FixtureStateCondition[T] =
      SetOfStatesAndAspects[T](stateInfo, s.toSet, neededStateAspects);

  def withAnyState: FixtureStateCondition[T] = 
      SetOfStatesAndAspects[T](stateInfo, 
                                stateInfo.allStates.asInstanceOf[Set[T#State]],
                                neededStateAspects);

  def withUndefinedState: FixtureStateCondition[T] = this;

  def withStateAspects(s: Seq[T#Aspect]): FixtureStateCondition[T] =
      SetOfStatesAndAspects[T](stateInfo, 
                                allowedStartStates,
                                s.toSet);

  def and(other: FixtureStateCondition[T]) = this;
  def or(other: FixtureStateCondition[T]) = other;

}

case class SetOfStatesAndAspects[T <: FixtureStateTypes](
                        override val stateInfo: T,
                        override val allowedStartStates: Set[T#State],
                        override val neededStateAspects: Set[T#Aspect]) 
                            extends FixtureStateCondition(stateInfo)
{

  def stateToLoad: T#State = 
      if (allowedStartStates.isEmpty) {
         throw new IllegalStateException("NoState have no state to load");
      } else {
         allowedStartStates.head
      }

  def addState(s: T#State) = SetOfStatesAndAspects[T](stateInfo,
                                                                 allowedStartStates + s,
                                                                 neededStateAspects);

  def withStartState(s: T#State): FixtureStateCondition[T] = 
      SetOfStatesAndAspects[T](stateInfo, allowedStartStates+s, neededStateAspects);
 
  def withAnyState: FixtureStateCondition[T]=
      SetOfStatesAndAspects[T](stateInfo, 
                                stateInfo.allStates.asInstanceOf[Set[T#State]],
                                neededStateAspects);

  def withStartStates(s: Seq[T#State]): FixtureStateCondition[T] =
      SetOfStatesAndAspects[T](stateInfo, s.toSet, neededStateAspects);

  def withUndefinedState: FixtureStateCondition[T] = 
      SetOfStatesAndAspects[T](stateInfo, Set(), neededStateAspects);

  def withStateAspects(s: Seq[T#Aspect]): FixtureStateCondition[T] =
      SetOfStatesAndAspects[T](stateInfo, 
                                allowedStartStates,
                                s.toSet);

  def and(other: FixtureStateCondition[T]): FixtureStateCondition[T] = 
   other match {
     case AnyState(_)  => this
     case NoState(_) => other
     case SetOfStatesAndAspects(x,s,a)  => SetOfStatesAndAspects[T](stateInfo, 
                          allowedStartStates intersect s,
                          neededStateAspects union a
                                                        )
   }
                       
  def or(other: FixtureStateCondition[T]) = 
   other match {
     case AnyState(_) => other
     case NoState(_) => this
     case SetOfStatesAndAspects(x,s,a) => SetOfStatesAndAspects[T](stateInfo, 
                                           allowedStartStates union s,
                                           neededStateAspects intersect a
                                                        )
   }

}

// vim: set ts=4 sw=4 et:
