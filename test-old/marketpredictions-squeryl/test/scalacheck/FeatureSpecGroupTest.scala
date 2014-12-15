package marketpredictions.test.scalacheck

import org.scalatest._
import marketpredictions.test._

/**
 * placeholder where all feature spec will run.
 */
class FeatureSpecGroupTest extends managedfixture.FeatureSpecGroup[MPFixtureStateTypes.type]
{

   val fixtureStateTypes = MPFixtureStateTypes;
   val fixtureAccess = MPFixtureAccess;

}

// vim: set ts=4 sw=4 et:
