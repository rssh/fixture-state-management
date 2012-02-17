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

  sealed trait FixtureStateAfterAny
  {
    def afterAny(v: DSLExpression): DSLExpression
  }

  sealed trait FixtureStateBeforeAny
  {
    this: DSLExpression =>
    def any(afterAny:FixtureStateAfterAny) = afterAny.afterAny(this)
  }

  class FixtureStateVerb extends DSLExpression
                           with FixtureStateBeforeAny
  {
    val value = TestFixtureStateUsageDescription.apply[T](fixtureStateTypes);

    def any() = new FixtureStateVerbAnyExpression(this)
  }

  val state = new FixtureStateVerbState

  class FixtureStateVerbState extends FixtureStateAfterAny
  {
    def afterAny(v: DSLExpression) =
                  new FixtureStateVerbStateExpression(v);
  }

  class FixtureStateVerbStateExpression(val upverb: DSLExpression) extends DSLExpression
                           with FixtureStateBeforeAny
  {
    def value = upverb.value.withAnyState;
  }

  class FixtureStateVerbAnyExpression(val upverb: DSLExpression) extends DSLExpression
  {
    def value = upverb.value.copy[T](precondition=AnyState[T](fixtureStateTypes));
  }

}

// vim: set ts=4 sw=4 et:
