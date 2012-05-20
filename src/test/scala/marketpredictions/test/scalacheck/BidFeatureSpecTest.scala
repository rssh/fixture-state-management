package marketpredictions.test.scalacheck

import org.scalatest._
import marketpredictions.test._

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

class BidFeatureSpecTest extends managedfixture.FeatureSpec[MPFixtureStateTypes.type]
{

   val fixtureStateTypes = MPFixtureStateTypes;
   val fixtureAccess = MPFixtureAccess;

   import MPFixtureStateTypes.DBStates._

   feature("bidding") {
      
       start state(S3_MORE_PREDICTIONS) 
       scenario("can't bid more money than user have") {
          api => 
           val bob = api.findUser("bob").get
           val event = api.findEvent("ab").get
           intercept[Exception] {
              api.bid(bob.id, event.id, 1, bob.balance+1.0);
           }
       }

       start state(S3_MORE_PREDICTIONS) 
       scenario("when only one bid, money is returned except comission") {
          api => 
           var bob = api.findUser("bob").get
           val event = api.findEvent("ab").get
           api.bid(bob.id, event.id, 1, bob.balance);
           bob = api.findUser("bob").get
           assert(bob.balance < BigDecimal(0.0001));
           api.setClockAndPrediction(CalendarUtil.addDays(event.passTime,1),
                                     event.id,
                                     0);
           // now bob must have balance greater than zeor
           bob = api.findUser("bob").get
           assert(bob.balance > BigDecimal(0.0));
       }

   }


}

// vim: set ts=4 sw=4 et:
