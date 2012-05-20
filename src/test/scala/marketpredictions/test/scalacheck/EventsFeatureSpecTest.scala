package marketpredictions.test.scalacheck

import org.scalatest._
import marketpredictions.test._

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

class EventsFeatureSpecTest extends managedfixture.FeatureSpec[MPFixtureStateTypes.type]
{

   //execution autonomic
 
   val fixtureStateTypes = MPFixtureStateTypes;
   val fixtureAccess = MPFixtureAccess;

   import MPFixtureStateTypes.DBStates._

   feature("events creation") {
      
       start state(S3_MORE_PREDICTIONS) change nothing
       scenario("it it not possible to create event with one alternative") {
          api => 
           val alice = api.findUser("alice").get
           intercept[IllegalArgumentException] {
            api.createEvent(alice.id, "t1", "test event", 1, CalendarUtil.addDays(api.now,1))
           }
       }

       start state(S3_MORE_PREDICTIONS) finish state(undefined)
       scenario("it is possible to create event with two alternatives") {
          api => 
            val alice = api.findUser("alice").get
            val id=api.createEvent(alice.id, "t2",  "test event 2", 2, CalendarUtil.addDays(api.now,1))
            assert(api.findEvent(id).isDefined);
       }

       start state(any) change(nothing) 
       scenario("it is impossible to create event with unexistend id") {
          api => 
           intercept[Exception] {
            val id=api.createEvent(-1,  "t3", "test event 3", 2, CalendarUtil.addDays(api.now,1))
           }
       }

   }


}

// vim: set ts=4 sw=4 et:
