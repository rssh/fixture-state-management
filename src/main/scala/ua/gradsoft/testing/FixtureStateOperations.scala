package ua.gradsoft.testing


/**
 * information about possible states, on which tests depends on.
 **/
trait FixtureStateOperations[T <: FixtureStateTypes]
{

  type Types = T;
  type FixtureType = T#FixtureType;
  type StartStateType = T#StartStateType;

  /**
   * how to load given state: i.e. load database dump, initialize vars according, etc..
   **/
  def load(f: Option[FixtureType], s: StartStateType): FixtureType;

  /**
   * if fixture is resource, than  how to close one.
   **/
  def close(f: FixtureType): Unit = { }

}


// vim: set ts=4 sw=4 et:
