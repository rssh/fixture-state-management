package ua.gradsoft.scalatest.state



sealed abstract class StateCondition[SI <: FixtureStateInfo[_]](val stateInfo:SI)
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

  def and (other: StateCondition[SI]): StateCondition[SI];
  def or  (other: StateCondition[SI]): StateCondition[SI];

}


case class AnyState[SI <: FixtureStateInfo[_]](override val stateInfo:SI) 
                                             extends StateCondition[SI](stateInfo)
{

  def allowedStartStates: Set[SI#StartStateType] =
       stateInfo.startStates.values.asInstanceOf[Set[SI#StartStateType]];
  
  def usedStateAspects:Set[SI#StateAspectType] = Set();

  def stateToLoad: SI#StartStateType = stateInfo.startStates.values.head;

  def and(other: StateCondition[SI]) = other;
  def or(other: StateCondition[SI]) = this;

}

case class NoState[SI <: FixtureStateInfo[_]](override val stateInfo:SI) 
                                             extends StateCondition(stateInfo)
{
  def allowedStartStates: Set[SI#StartStateType] = Set();
  def usedStateAspects: Set[SI#StateAspectType] = 
                     stateInfo.stateAspects.values.asInstanceOf[Set[SI#StateAspectType]]

  def stateToLoad: SI#StartStateType = 
         throw new IllegalStateException("NoState have no state to load");

  def and(other: StateCondition[SI]) = this;
  def or(other: StateCondition[SI]) = other;

}

case class SetOfStatesAndAspects[SI <: FixtureStateInfo[_]](
                        override val stateInfo: SI,
                        override val allowedStartStates: Set[SI#StartStateType],
                        override val usedStateAspects: Set[SI#StateAspectType]) 
                            extends StateCondition(stateInfo)
{

  def stateToLoad: SI#StartStateType = 
      if (allowedStartStates.isEmpty) {
         throw new IllegalStateException("NoState have no state to load");
      } else {
         allowedStartStates.head
      }


  def and(other: StateCondition[SI]): StateCondition[SI] = 
   other match {
     case AnyState(_)  => this
     case NoState(_) => other
     case SetOfStatesAndAspects(x,s,a)  => SetOfStatesAndAspects[SI](stateInfo, 
                          allowedStartStates intersect s,
                          usedStateAspects union a
                                                        )
   }
                       
  def or(other: StateCondition[SI]) = 
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
