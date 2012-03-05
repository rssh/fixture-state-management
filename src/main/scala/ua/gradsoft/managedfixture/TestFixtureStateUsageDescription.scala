package ua.gradsoft.managedfixture

/**
 * Description of one test.
 *@param stateTypes -- fixture state types.
 *@param precondition --  what situation must be before test.
 *                        Usually this is set of possible db states
 *@param startStateChange - how test change state
 *@param stateAspectsChanged - what aspects changed by this state if it runs succesfully. 
 *  By default - All. If stateAspectsChanged is empty set, than this test does not change
 *  aspects at all.
 **/
case class TestFixtureStateUsageDescription[T <: FixtureStateTypes](
                         val stateInfo: T,
                         val precondition: FixtureStateCondition[T],
                         val startStateChange: FixtureStateChange[T],
                         val stateAspectsChanged: Set[T#StateAspectType],
                         val canRunParallel: Boolean)
{

  def withAnyState: TestFixtureStateUsageDescription[T] =
             copy[T](precondition=precondition.withAnyState);

  def withUndefinedState: TestFixtureStateUsageDescription[T] =
             copy[T](precondition=precondition.withUndefinedState);

  def withStartState(s: T#StartStateType): TestFixtureStateUsageDescription[T] =
             copy[T](precondition=precondition.withStartState(s));

  def withStartStates(s: Seq[T#StartStateType]): TestFixtureStateUsageDescription[T] =
             copy[T](precondition=precondition.withStartStates(s));

  def withStateAspects(s: Seq[T#StateAspectType]): TestFixtureStateUsageDescription[T] =
             copy[T](precondition=precondition.withStateAspects(s));

  def withFinishState(s: T#StartStateType): TestFixtureStateUsageDescription[T] =
             copy[T](startStateChange = NewState[T](s) );

  def withFinishStateUndefined: TestFixtureStateUsageDescription[T] =
             copy[T](startStateChange = UndefinedState );

  def withChangeNothing: TestFixtureStateUsageDescription[T] =
             copy[T](startStateChange = SameState,
                     stateAspectsChanged = Set()
                    );

}


                     
object TestFixtureStateUsageDescription
{

  /**
   * create state usage description with default values, which can be used for
   * bulding of more complicated descriptions.
   **/
  def apply[T <: FixtureStateTypes](stateInfo:T) =
      {
           if (stateInfo==null) {
              throw new IllegalArgumentException("stateInfo must not be null");
           }
           new TestFixtureStateUsageDescription[T](
                         stateInfo = stateInfo,
                         precondition = NoState[T](stateInfo),
                         startStateChange = UndefinedState,
                         stateAspectsChanged = 
                            stateInfo.stateAspects.values.asInstanceOf[Set[T#StateAspectType]],
                         canRunParallel = false
           );
      }


}


// vim: set ts=4 sw=4 et:
