package ua.gradsoft.scalatest.state


/**
 * information about possible states, on which tests depends on.
 **/
trait FixtureStateInfo[SelfType <: FixtureStateInfo[_]]
{

  this: SelfType =>

  /**
   * type of fixture, for which we manage states.
   * This can be db link (represented by jdbc connection)
   **/
  type FixtureType ;

  /**
   * Set of possible start states, wich can be loaded.
   **/
  val startStates: Enumeration;

  type StartStateType = startStates.Value;

  /**
   * how to load given state: i.e. load database dump, initialize vars according, etc..
   **/
  def load(f: Option[SelfType#FixtureType], s: SelfType#StartStateType): SelfType#FixtureType;

  /**
   * if fixture is resource, than  how to close one.
   **/
  def close(f: SelfType#FixtureType): Unit;

  /**
   * Set of possible state aspects. Aspect here is some part of state: test can use or change different
   * aspects of same state. Individuial test can change some of those aspects. This means, that
   * other test, which use different aspects than first, can run after first without state reloading.
   **/
  val stateAspects: Enumeration = FixtureStateInfo.OneAspectForAll;

  type StateAspectType = stateAspects.Value;

}

object FixtureStateInfo
{

  object OneAspectForAll extends Enumeration
  {
    val ALL = Value;
  }

}

// vim: set ts=4 sw=4 et:
