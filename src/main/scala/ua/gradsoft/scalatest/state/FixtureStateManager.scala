package ua.gradsoft.scalatest.state

class FixtureStateManager[SI <: FixtureStateInfo[_]](val stateInfo: FixtureStateInfo[SI])
{

  var fixture: Option[SI#FixtureType] = None;
  var currentStartState: Option[SI#StartStateType] = None;
  var usedStateAspects: Set[SI#StateAspectType] = Set();

  def doWith(statePrecondition: StateCondition[SI],
             aspectsToUse:Set[SI#StateAspectType], 
             stateChange: StateChange[SI],
             f: SI#FixtureType => Unit): Unit =
  {
    stateInfo.synchronized {
       if (currentStartState==None) {
           loadState(statePrecondition.stateToLoad);
       }else if(!statePrecondition.check(currentStartState.get)) {
           loadState(statePrecondition.stateToLoad);
       }
       if (!(usedStateAspects intersect aspectsToUse).isEmpty) {
           loadState(statePrecondition.stateToLoad);
       }
       f(fixture.get);
       usedStateAspects = (usedStateAspects union aspectsToUse);
       stateChange match {
         case SameState => /* do nothing */
         case NewState(x) => { currentStartState = Some(x); 
                               usedStateAspects = Set(); }
         case UndefinedState => { stateInfo.close(fixture.get); 
                                  currentStartState=None;
                                  fixture=None;
                                }
       }
    }
  }

  private[this] def loadState(s: SI#StartStateType) {
      fixture = Some(stateInfo.load(fixture,s));
      currentStartState = Some(s); 
      usedStateAspects = Set();
  }

}

// vim: set ts=4 sw=4 et:
