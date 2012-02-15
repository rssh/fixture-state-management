package ua.gradsoft.scalatest.state

/**
 * test with states: 
 **/
trait TestOnStatefullFixture
{

   /**
    * states, on which test is work.
    **/
   val fixtureStateInfo: FixtureStateInfo;

   /**
    * preconditions, i.e. what precnditions is needed to
    * be before test.
    * Usually this is set of possible db states or empty preconditions,
    * if we can 
    **/
   def preconditions: StateConditions;


   /**
    * postcondition, i.e. on what we can hope after test
    **/
   def postconditions: StateConditions = NoState(fixtureStateInfo);

   /**
    * what aspects changed by this state if it runs succesfully. By default - All.
    * If aspectsChanged == None than this test does not change
    * state at all. (for example create and then remove object).
    **/
   def stateAspectsChanged: Set[fixtureStateInfo.StateAspectType] = fixtureStateInfo.stateAspects.values;



}

// vim: set ts=4 sw=4 et:
