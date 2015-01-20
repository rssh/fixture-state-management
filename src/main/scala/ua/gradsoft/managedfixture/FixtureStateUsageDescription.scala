package ua.gradsoft.managedfixture


 /**
  * Description of one test.
  *@param precondition --  what situation must be before test.
  *                        Usually this is set of possible db states
  *@param startStateChange - how test change state
  **/
case class FixtureStateUsageDescription[State](
                         val precondition: FixtureStateCondition[State],
                         val startStateChange: FixtureStateChange[State],
                         val canRunParallel: Boolean)
 {

  def withAnyState: FixtureStateUsageDescription[State] =
             copy(precondition=AnyState);

  def withUndefinedState: FixtureStateUsageDescription[State] =
             copy(precondition=States(Set()));

  def withStartState(s: State): FixtureStateUsageDescription[State] =
             copy(precondition=precondition + s);

  def withStartStates(s: Seq[State]): FixtureStateUsageDescription[State] =
             copy(precondition=precondition ++ s.toSet);

  def withFinishState(s: State): FixtureStateUsageDescription[State] =
             copy(startStateChange = NewState(s) );

  def withFinishStateUndefined: FixtureStateUsageDescription[State] =
             copy(startStateChange = UndefinedState );

  def withChangeNothing: FixtureStateUsageDescription[State] =
             copy(startStateChange = SameState)

  def withParallel(flag:Boolean): FixtureStateUsageDescription[State] =
             copy(canRunParallel = flag)


}


                     
object FixtureStateUsageDescription
{

  /**
   * create state usage description with default values, which can be used for
   * bulding of more complicated descriptions.
   **/
  def apply[State](): FixtureStateUsageDescription[State] =
           new FixtureStateUsageDescription(
                         precondition = States.empty,
                         startStateChange = UndefinedState,
                         canRunParallel = false
           )


}


// vim: set ts=4 sw=4 et:
