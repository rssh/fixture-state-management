package ua.gradsoft.managedfixture


sealed trait FixtureStateCondition[+State]
{
   def +[InState >: State] (s: InState): FixtureStateCondition[InState]
   def +[InState >: State] (s: FixtureStateCondition[InState]): FixtureStateCondition[InState]
   def ++[InState >: State](s: Set[InState]): FixtureStateCondition[InState]
}

object AnyState extends FixtureStateCondition[Nothing]
{
  override def +[InState] (s: InState) = this
  override def +[InState] (s: FixtureStateCondition[InState]) = this
  override def ++[InState](s: Set[InState]) = this
}

case class States[State](states: Set[State]) extends FixtureStateCondition[State]
{
  override def +[InState >: State] (s: InState) = States(states ++ Set(s))
  override def +[InState >: State] (s: FixtureStateCondition[InState]) =
             s match {
                 case AnyState => AnyState
                 case States(otherStates) => States(states ++ otherStates.asInstanceOf[Set[InState]])
             }
  override def ++[InState >: State](s: Set[InState]) = 
                 States(states ++ s)
}

object States
{
  def empty = States(Set())
}

