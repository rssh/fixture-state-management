package ua.gradsoft.scalatest.state


sealed trait FixtureStateChange[+T <: FixtureStateTypes];

case object SameState extends FixtureStateChange[Nothing];

case class NewState[+T <: FixtureStateTypes](val state: T#StartStateType) extends FixtureStateChange[T];

case object UndefinedState extends FixtureStateChange[Nothing];


// vim: set ts=4 sw=4 et:
