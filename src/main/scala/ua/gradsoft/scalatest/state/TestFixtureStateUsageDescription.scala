package ua.gradsoft.scalatest.state

/**
 * Description of one test.
 **/
abstract class TestFixtureStateUsageDescription[T <: FixtureStateTypes](stateInfo: T)
{

   /**
    * precondition, i. e. what situation must be before test.
    * Usually this is set of possible db states
    **/
   def precondition: FixtureStateCondition[T];


   /**
    * changes - what start changes 
    **/
   def startStateChange: FixtureStateChange[T] = UndefinedState;

   /**
    * what aspects changed by this state if it runs succesfully. By default - All.
    * If aspectsChanged == None than this test does not change
    * state at all. (for example create and then remove object).
    **/
   def stateAspectsChanged: Set[T#StateAspectType] = 
            stateInfo.stateAspects.values.asInstanceOf[Set[T#StateAspectType]];

}

// vim: set ts=4 sw=4 et:
