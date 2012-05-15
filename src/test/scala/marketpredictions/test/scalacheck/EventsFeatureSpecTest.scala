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
            api.createEvent(alice.id, "test event", 1, CalendarUtil.addDays(api.now,1))
           }
       }

   }


}

// vim: set ts=4 sw=4 et:
