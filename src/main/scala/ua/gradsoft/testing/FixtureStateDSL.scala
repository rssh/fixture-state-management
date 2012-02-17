package ua.gradsoft.testing

/**
 * DLS for fixture state
 * Usage: for fixture stat
 * @{{{
 *   `fixture state` any ();
 *   `fixture state` any 
 *   `fixture state` any state
 *   require state  <state-name>
 *   require states (<list-of-state-names>)
 *
 *   require clear aspects
 * }}}@
 *
 **/
trait FixtureStateDSL[T <: FixtureStateTypes]
{

  def fixtureStateTypes: T


  trait DSLExpression
  {
    def value: TestFixtureStateUsageDescription[T]
  }

  class FixtureStateVerb extends DSLExpression
  {
    val value = TestFixtureStateUsageDescription.apply[T](fixtureStateTypes);

    def any = new FixtureStateVerbAny(this);
  }

  class FixtureStateVerbAny(val upverb: DSLExpression)
  {
    def state = new FixtureStateVerbAnyState(upverb);
  }

  class FixtureStateVerbAnyState(val upverb: DSLExpression) extends DSLExpression
  {

    def value = upverb.value.withAnyState;

  }

}

// vim: set ts=4 sw=4 et:
