package ua.gradsoft.managedfixture


/**
 * information about possible states on which tests depends on.
 */
trait FixtureStateTypes
{

  /**
   * type of fixture, for which we manage states.
   * (i.e. db link for databases, etc). 
   **/
  type FixtureType ;

  /**
   * Set of possible start states, wich can be loaded.
   **/
  val startStates: Enumeration;

  /**
   * Enumeration type for startStates
   **/
  type StartStateType = startStates.Value;

  /**
   * Set of possible state aspects. Aspect here is some part of state: test can use or change different
   * aspects of same state. I.e. if one test use one set of aspects, than other test, which use different 
   * aspects than first, can run after first without reloading of fixture state.
   *
   * By default all tests use one global aspect.
   **/
  val stateAspects: Enumeration = FixtureStateTypes.OneAspectForAll;

  /**
   * Enumearation type for stateAspects
   **/
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
