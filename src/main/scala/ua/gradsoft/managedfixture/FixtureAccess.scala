package ua.gradsoft.managedfixture

import scala.concurrent.Lock;

/**
 * Test authors must implement this trait for wrapping access to managed fixture.
 **/
trait FixtureAccess[T <: FixtureStateTypes]
{

  type Types = T;
  type FixtureType = T#FixtureType;
  type StartStateType = T#StartStateType;
  type StateAspectType = T#StateAspectType;

  /**
   * how to load given state: i.e. load database dump, initialize vars according, etc..
   * after call of load, current must return fixture with access to given state
   **/
  def load(s: StartStateType);


  /**
   * retrive information about current state if possible.
   **/
  def current:Option[(StartStateType,Set[StateAspectType])]
    = None;

  /**
   * used to mark state changes if wer track changes in datastore.
   * Common approach is to have special test-specifics tables in relational database
   * and check one in load state.
   * By default -- do nothing.
   **/
  def markStateChanges(stateChange: FixtureStateChange[T], stateAspectsChanges: Set[StateAspectType])
  {  }

  /**
   * get current value of fixtire.  This value can be used by fixtuee
   *@return fixture wich represent current state or Nothing, if current state is
   *        not defined.
   **/
  def acquire(): Option[FixtureType];


  /**
   * if fixture is resource, than close one.
   **/
  def release(f: FixtureType): Unit = { }

  /**
   * Suite-level lock. If this method return Some(lock), than each suite is executed in scope 
   * of this lock, otherwise tests in different suites can execute concurrently.
   * By default returned in lock with lifecicle same as FixtureAccess. Override this if you want
   * other behavior.
   **/
  def suiteLevelLock: Option[Lock]
    =  Some(_suiteLevelLock)

  private lazy val _suiteLevelLock = new Lock();
  
  /**
   * lock for squence level locks. (i.e. set of tests inside one suite (usually - one test))
   * which need specific resource state and can be executed in parallel.
   * By default returned in lock with lifecicle same as FixtureAccess. Override this if you want
   * other behavior.  Note, that sute lock can not be the same object as suite lock.
   **/
  def testLevelLock: Option[Lock]
      = Some(_testLevelLock)
  
  private lazy val _testLevelLock = new Lock();

}


// vim: set ts=4 sw=4 et:
