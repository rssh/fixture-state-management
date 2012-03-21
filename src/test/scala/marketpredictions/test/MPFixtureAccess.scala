package marketpredictions.test

import java.sql._;
import org.squeryl._;
import org.squeryl.adapters._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;

import ua.gradsoft.managedfixture._

import marketpredictions.db._
import marketpredictions.engine._

object MPFixtureAccess extends FixtureAccess[MPFixtureStateTypes.type]
                        with RdbAccessHelper[MPFixtureStateTypes.type]
{

  val fixtureStateTypes = MPFixtureStateTypes;

  def acquire() = Some(testApi)

  def acquireJdbcConnection = testApi.sqlConnection;

  import MPFixtureStateTypes.DBStates._
  def load(s: StartStateType): Unit =
  {
    current match {
      case None => recreateDB();
                   loadFromState(EMPTY,s);
      case Some(s0) => loadFromState(s0._1,s);
    }
    markStateChanges(NewState[MPFixtureStateTypes.type](s),Set());
  }

  private def recreateDB()
  {
    // fully recreate database to empty state.
    val api = acquire(); // to init squeryl
    inTransaction {
      MPSchema.preClear
      MPSchema.drop
      MPSchema.create
      MPSchema.postInit
      createTestStatesTableIfNeeded
    }
  }

  private def loadFromState(stateFrom: StartStateType, stateTo: StartStateType): Unit =
  {
    def transitions = Map[Int,()=>Unit](
                         EMPTY.id -> { () =>  
                                      testApi.now = CalendarUtil.timestamp(2012,01,01,00,00);
                                      testApi.createUser("alice");
                                      testApi.createUser("bob");     
                                      testApi.createUser("alex");     
                                   } ,
                         S1_USERS_WITHOUT_MONEY.id ->  { () =>
                                      testApi.now = CalendarUtil.timestamp(2012,02,01,00,00);
                                      val alice = testApi.findUser("alice").get;
                                      val bob = testApi.findUser("bob").get;
                                      val alex = testApi.findUser("alex").get;
                                      testApi.payIn(alice.id,BigDecimal(100L));
                                      testApi.payIn(bob.id,BigDecimal(100L));
                                      testApi.payIn(alex.id,BigDecimal(100L));
                                   },
                         S2_MONEY.id ->  { () =>
                                      val alice = testApi.findUser("alice").get;
                                      // alice open auction on 100
                                      testApi.now = CalendarUtil.timestamp(2012,02,01,00,00);
                                      testApi.createPrediction( alice.id , 
                                        "A and B are sitting on the tube. Who will left fisrt. [0-A,1-B]",
                                        2,
                                        CalendarUtil.timestamp(2012,03,01,00,00),
                                        200);
                                      }
                      );
    if (stateFrom.id < stateTo.id) {
       for(x <- stateFrom.id until stateTo.id) {
         transitions(x).apply();
       }
    } else {
       recreateDB();
       loadFromState(EMPTY,stateTo);
    }
  }
  
  
  // here will be our shared container.
  lazy val testApi = { val retval = new TestApi(new Timestamp(0L));
                       retval.onInit;
                       retval;
                     }

}

// vim: set ts=4 sw=4 et:
