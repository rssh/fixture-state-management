package ua.gradsoft.testing


/**
 * Test authors must implement this trait for accessing
 * managed structures.
 **/
trait FixtureAccess[T <: FixtureStateTypes]
{

  type Types = T;
  type FixtureType = T#FixtureType;
  type StartStateType = T#StartStateType;

  /**
   * how to load given state: i.e. load database dump, initialize vars according, etc..
   * after call of load, current must return fixture with access to given state
   **/
  def load(s: StartStateType);

  /**
   * get current value of fix
   *@return fixture wich represent current state or Nothing, if current state is
   *        not defined.
   **/
  def current: Option[FixtureType];


  /**
   * if fixture is resource, than close one.
   **/
  def close(): Unit = { }

}


// vim: set ts=4 sw=4 et:
