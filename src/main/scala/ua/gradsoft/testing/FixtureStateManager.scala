package ua.gradsoft.testing

class FixtureStateManager[T <: FixtureStateTypes](val fixtureAccess: FixtureAccess[T])
{

  var currentStartState: Option[T#StartStateType] = None;
  var usedStateAspects: Set[T#StateAspectType] = Set();

  def doWith(usage: TestFixtureStateUsageDescription[T],
             f: T#FixtureType => Unit): Unit =
  {
    fixtureAccess.synchronized {
       if (currentStartState==None) {
           loadState(usage.precondition.stateToLoad);
       }else if(!usage.precondition.check(currentStartState.get)) {
           loadState(usage.precondition.stateToLoad);
       }
       if (!(usedStateAspects intersect usage.precondition.neededStateAspects).isEmpty) {
           loadState(usage.precondition.stateToLoad);
       }
       try {
        fixtureAccess.current match {
         case Some(fixture) => f(fixture)
         case None => throw new IllegalStateException("FixturAccess does not return reference to loaded structure");
        }
       } finally {
         usedStateAspects = (usedStateAspects union usage.stateAspectsChanged);
         usage.startStateChange match {
           case SameState => /* do nothing */
           case NewState(x) => { currentStartState = Some(x); 
                                 usedStateAspects = Set(); }
           case UndefinedState => {  
                                    currentStartState=None;
                                  }
         }
       }
    }
  }

  private[this] def loadState(s: T#StartStateType) {
      fixtureAccess.load(s);
      currentStartState = Some(s); 
      usedStateAspects = Set();
  }

}

// vim: set ts=4 sw=4 et:
