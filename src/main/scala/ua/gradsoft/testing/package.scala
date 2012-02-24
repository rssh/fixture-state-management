package ua.gradsoft

/**
  *Provides building block for externally management fixtures (such as databases).
  *
  *Managed fixture defined by 
  *<ul>
  *<li> [[ua.gradsoft.testing.FixtureStateTypes]] with definitions for type of fixture and enumeration of states, which can be loaded into fixture (we name this start states) and optional enumeration of state aspects.
  *</li>
  *<li> [[ua.gradsoft.testing.FixtureAccess]] with operations under fixture state, such as - load given state,
  *  aquire fixture instance, etc.
  *</li></ul>
  *
  *After defining managed fixture we can use one in tests by attaching to each fixture test description
  *  of required states and changed to this states on simple DSL (see [[ua.gradsoft.testing.FixtureStateDSL]] ).
  *  Test execution engine can use this information for splitting tests into sequence of parts, which can
  *  be executed in parallel, calculation of order of execution of those parts and running ones in this order.
  *
  *Integration with popular scalatest package is available in [[org.scalatest.managedfixture]].
  */
package object testing {

}

// vim: set ts=4 sw=4 et:
