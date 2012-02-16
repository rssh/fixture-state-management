package ua.gradsoft.scalatest.state


sealed trait StateChange[+T <: FixtureStateInfo[_]];

case object SameState extends StateChange[Nothing];

case class NewState[+T <: FixtureStateInfo[_]](val state: T#StartStateType) extends StateChange[T];


case object UndefinedState extends StateChange[Nothing];


// vim: set ts=4 sw=4 et:
