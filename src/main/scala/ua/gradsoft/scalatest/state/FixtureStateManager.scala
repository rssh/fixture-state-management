package ua.gradsoft.scalatest.state

class FixtureStateManager[T <: FixtureStateTypes](val stateOps: FixtureStateOperations[T])
{

  var fixture: Option[T#FixtureType] = None;
  var currentStartState: Option[T#StartStateType] = None;
  var usedStateAspects: Set[T#StateAspectType] = Set();

  def doWith(statePrecondition: FixtureStateCondition[T],
             aspectsToUse:Set[T#StateAspectType], 
             stateChange: FixtureStateChange[T],
             f: T#FixtureType => Unit): Unit =
  {
    stateOps.synchronized {
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
         case UndefinedState => { stateOps.close(fixture.get); 
                                  currentStartState=None;
                                  fixture=None;
                                }
       }
    }
  }

  private[this] def loadState(s: T#StartStateType) {
      fixture = Some(stateOps.load(fixture,s));
      currentStartState = Some(s); 
      usedStateAspects = Set();
  }

}

// vim: set ts=4 sw=4 et:
