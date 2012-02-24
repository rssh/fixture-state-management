package org.scalatest

/**
  * Extensions to scalatest for running tests with managed fixtures.
  * according to next pattern:
  *
  * {{{
  * import ua.gradsoft.testing._
  * import org.scalates.managedfixture._
  *  
  * object MyFixtureStateTypes extends FixtureStateTypes
  * {
  *  ....
  * }
  *
  * object MyFixtureAccess extends FixtureAccess
  * {
  *  ....
  * }
  *
  * class MySuite extents managedfixture.Something[MyFixtureStateTypes.type]
  * {
  *
  *     val fixtureStateTypes = MyFixtureStateTypes
  *     val fixtureAccess = MyFixtureAccess
  *  
  *     start state(S1) finish state undefined 
  *     .... tests, which needed state s1
  *
  *     start state(S2) change unchanged parallel
  *     .... tests, which needed state s2 and can be executed in parallel
  *
  * }
  * }}}
  *
  * For description of idea behind managedfixture see [[ua.gradsoft.testing]],
  * [[ua.gradsoft.testing.FixtureStateTypes]], [[ua.gradsoft.testing.FixtureAccess]]
  *
  * Next types of suites have version with managedfixture support:
  * <ul>
  *  <li> [[org.scalatest.managedfixture.FunSuite]] </li>
  *  <li> [[org.scalatest.managedfixture.FlatSpec]] </li>
  *  <li> [[org.scalatest.managedfixture.FreeSpec]] </li>
  *  <li> [[org.scalatest.managedfixture.FeatureSpec]] </li>
  *  <li> [[org.scalatest.managedfixture.PropSpec]] </li>
  * </ul>
  *
  */
package object managedfixture
{


}

// vim: set ts=4 sw=4 et:
