package ua.gradsoft.testing


/**
 * StateCondition - what states is needed and what aspects must be cleaned.
 **/
sealed abstract class FixtureStateCondition[SI <: FixtureStateTypes](val stateInfo:SI)
{

  
  /**
   * check when state satisficy position
   **/
  def check(s:SI#StartStateType):Boolean = 
                                 allowedStartStates.contains(s);

  /**
   * set of states, from which test can be runned.
   **/
  def allowedStartStates:Set[SI#StartStateType] ;

  /**
   * set of state aspects, which must be clean
   **/
  def usedStateAspects: Set[SI#StateAspectType] ;

  /**
   * Choose one state from correct, which is loaded by engine
   * to start this preconditions.
   **/
  def stateToLoad: SI#StartStateType;

  /**
   *  return condition wiht added startState s.
   **/
  def withStartState(s: SI#StartStateType): FixtureStateCondition[SI];

  /**
   * return condition where input state can be any (and used aspects same
   * as current)
   **/
  def withAnyState: FixtureStateCondition[SI];

  def and (other: FixtureStateCondition[SI]): FixtureStateCondition[SI];
  def or  (other: FixtureStateCondition[SI]): FixtureStateCondition[SI];

}


case class AnyState[SI <: FixtureStateTypes](override val stateInfo:SI) 
                                             extends FixtureStateCondition[SI](stateInfo)
{

  def allowedStartStates: Set[SI#StartStateType] =
       stateInfo.startStates.values.asInstanceOf[Set[SI#StartStateType]];
  
  def usedStateAspects:Set[SI#StateAspectType] = Set();

  def stateToLoad: SI#StartStateType = stateInfo.startStates.values.head;

  def withStartState(s: SI#StartStateType): FixtureStateCondition[SI] = this;

  def withStartStates(s: Seq[SI#StartStateType]): FixtureStateCondition[SI] = this;

  def withAnyState: FixtureStateCondition[SI] = this;

  def and(other: FixtureStateCondition[SI]) = other;
  def or(other: FixtureStateCondition[SI]) = this;

}

case class NoState[SI <: FixtureStateTypes](override val stateInfo:SI) 
                                             extends FixtureStateCondition(stateInfo)
{
  def allowedStartStates: Set[SI#StartStateType] = Set();
  def usedStateAspects: Set[SI#StateAspectType] = 
                     stateInfo.stateAspects.values.asInstanceOf[Set[SI#StateAspectType]]

  def stateToLoad: SI#StartStateType = 
         throw new IllegalStateException("NoState have no state to load");

  def withStartState(s: SI#StartStateType): FixtureStateCondition[SI] = 
      SetOfStatesAndAspects[SI](stateInfo, Set(s), usedStateAspects);

  def withAnyState: FixtureStateCondition[SI] = 
      SetOfStatesAndAspects[SI](stateInfo, 
                                stateInfo.startStates.values.asInstanceOf[Set[SI#StartStateType]],
                                usedStateAspects);

  def and(other: FixtureStateCondition[SI]) = this;
  def or(other: FixtureStateCondition[SI]) = other;

}

case class SetOfStatesAndAspects[SI <: FixtureStateTypes](
                        override val stateInfo: SI,
                        override val allowedStartStates: Set[SI#StartStateType],
                        override val usedStateAspects: Set[SI#StateAspectType]) 
                            extends FixtureStateCondition(stateInfo)
{

  def stateToLoad: SI#StartStateType = 
      if (allowedStartStates.isEmpty) {
         throw new IllegalStateException("NoState have no state to load");
      } else {
         allowedStartStates.head
      }

  def addState(s: SI#StartStateType) = SetOfStatesAndAspects[SI](stateInfo,
                                                                 allowedStartStates + s,
                                                                 usedStateAspects);

  def withStartState(s: SI#StartStateType): FixtureStateCondition[SI] = 
      SetOfStatesAndAspects[SI](stateInfo, allowedStartStates+s, usedStateAspects);
 
  def withAnyState: FixtureStateCondition[SI]=
      SetOfStatesAndAspects[SI](stateInfo, 
                                stateInfo.startStates.values.asInstanceOf[Set[SI#StartStateType]],
                                usedStateAspects);


  def and(other: FixtureStateCondition[SI]): FixtureStateCondition[SI] = 
   other match {
     case AnyState(_)  => this
     case NoState(_) => other
     case SetOfStatesAndAspects(x,s,a)  => SetOfStatesAndAspects[SI](stateInfo, 
                          allowedStartStates intersect s,
                          usedStateAspects union a
                                                        )
   }
                       
  def or(other: FixtureStateCondition[SI]) = 
   other match {
     case AnyState(_) => other
     case NoState(_) => this
     case SetOfStatesAndAspects(x,s,a) => SetOfStatesAndAspects[SI](stateInfo, 
                                           allowedStartStates union s,
                                           usedStateAspects intersect a
                                                        )
   }

}

// vim: set ts=4 sw=4 et:
