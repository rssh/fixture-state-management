package marketpredictions.test

import ua.gradsoft.managedfixture._

object MPFixtureStateTypes extends FixtureStateTypes
{

  type FixtureType = TestApi

  object DBStates extends Enumeration
  {
    val EMPTY = Value;
    val S1_USERS_WITHOUT_MONEY = Value; // added 3 users without maney
    val S2_USERS_WITH_MONEY = Value;  // added payments
    val S3_MORE_PREDICTIONS = Value; //  S2 with predictions
  }

  val startStates = DBStates;
  

}

// vim: set ts=4 sw=4 et:
