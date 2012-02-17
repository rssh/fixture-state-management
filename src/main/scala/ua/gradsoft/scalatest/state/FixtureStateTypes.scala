package ua.gradsoft.scalatest.state


/**
 * information about possible states, on which tests depends on.
 **/
trait FixtureStateTypes
{

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
   * Set of possible state aspects. Aspect here is some part of state: test can use or change different
   * aspects of same state. Individuial test can change some of those aspects. This means, that
   * other test, which use different aspects than first, can run after first without state reloading.
   **/
  val stateAspects: Enumeration = FixtureStateTypes.OneAspectForAll;

  type StateAspectType = stateAspects.Value;

}

object FixtureStateTypes
{

  object OneAspectForAll extends Enumeration
  {
    val ALL = Value;
  }

}

// vim: set ts=4 sw=4 et:
