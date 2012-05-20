package marketpredictions.test.scalacheck

import org.scalatest._
import marketpredictions.test._

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

class PaymentFeatureSpecTest extends managedfixture.FeatureSpec[MPFixtureStateTypes.type]
{

   val fixtureStateTypes = MPFixtureStateTypes;
   val fixtureAccess = MPFixtureAccess;

   import MPFixtureStateTypes.DBStates._

   feature("payment") {
      
       start state(S1_USERS_WITHOUT_MONEY) 
       scenario("payment must increase user balance") {
          api => 
           var bob = api.findUser("bob").get
           val sum = BigDecimal(100L);
           api.payIn(bob.id, sum);
           bob = api.findUser("bob").get
           assert(bob.balance >= sum);
       }

   }


}

// vim: set ts=4 sw=4 et:
