package ua.gradsoft.scalatest.state

trait StateConditions
{
  val stateInfo: FixtureStateInfo;

  def allowedStartStates: Set[stateInfo.StartStateType] ;
  def usedStateAspects:Set[stateInfo.StateAspectType] ;

}

case class AnyState(override val stateInfo:FixtureStateInfo)  extends StateConditions
{

  def allowedStartStates: Set[stateInfo.StartStateType] =
       stateInfo.startStates.values;
  
  def usedStateAspects:Set[stateInfo.StateAspectType] = Set();

}

case class NoState(override val stateInfo:FixtureStateInfo) extends StateConditions
{
  def allowedStartStates: Set[stateInfo.StartStateType] = Set();
  def usedStateAspects: Set[stateInfo.StateAspectType] = 
       stateInfo.stateAspects.values;
}


// vim: set ts=4 sw=4 et:
