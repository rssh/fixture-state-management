package ua.gradsoft.testing

/**
 * DLS for fixture state
 * Usage: for fixture state
 * Grammar
 * <pre>   
 * TestUsageDescription:  Mark ([StartDescription] | 
 *                              [FinishDescription] | 
 *                              [ChangeDescription])*
 *
 * StartDescription: start StartStateDescription [aspects (all | '(' {ID}* ')' | none) ]
 * FinishDescription: finish FinishStateDescription
 * CnangesDescription: change [nothing| all] 
 *
 * StartStateDescription: state (any | undefined | {ID} )
 *                       |
 *                        states({ID}*)
 *
 * FinishStateDescription: state (undefined | {ID} )
 *
 *
 * </pre>
 * @{{{
 *   `fixture state` start state(any)
 *   `fixture state` start state(any) change noting
 *   `fixture state` start state(any) finish state(undefined)
 *   `fixture state` start state(S3) finish state(S4)
 *   `fixture state` start states(S1,S2,S3) aspects (1,2,3) change none
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
                        with FixtureStateVerb_Start
  { 
    val value = TestFixtureStateUsageDescription[T](fixtureStateTypes);
  }
                          

  trait FixtureStateVerb_Start extends DSLExpression
  {

    def start(x: FixtureStateVerb_STATE0.type):FixtureStateVerbStartState0 =
          new FixtureStateVerbStartState0(this);

    def start(x: FixtureStateVerb_STATE_ANY.type):FixtureStateVerbStartStateAny =
          new FixtureStateVerbStartStateAny(this);

    def start(x: FixtureStateVerb_STATE_UNDEFINED.type):FixtureStateVerbStartStateUndefined =
          new FixtureStateVerbStartStateUndefined(this);

    def start(x: FixtureStateVerb_STATE): FixtureStateVerbStartState =
            new FixtureStateVerbStartState(this,x.s);

    def start(x: FixtureStateVerb_STATES) : FixtureStateVerbStartStates = 
            new  FixtureStateVerbStartStates(this,x.args);

  }
    

  case class FixtureStateVerb_STATE(val s:T#StartStateType);
  case object FixtureStateVerb_STATE0;
  case object FixtureStateVerb_ANY;
  case object FixtureStateVerb_UNDEFINED;
  case object FixtureStateVerb_STATE_ANY;
  case object FixtureStateVerb_STATE_UNDEFINED;
  case class FixtureStateVerb_STATES(val args:Seq[T#StartStateType]);

  val any = FixtureStateVerb_ANY;
  val undefined = FixtureStateVerb_UNDEFINED;
                
  def state(x:T#StartStateType) = FixtureStateVerb_STATE(x);
  val state = FixtureStateVerb_STATE0;
  def states(x:T#StartStateType*) = FixtureStateVerb_STATES(x);

  def state(x:FixtureStateVerb_ANY.type) = FixtureStateVerb_STATE_ANY;
  def state(x:FixtureStateVerb_UNDEFINED.type) = FixtureStateVerb_STATE_UNDEFINED;

  class FixtureStateVerbStartState0(up: DSLExpression)
  {
    def any: FixtureStateVerbStartStateAny
          = new FixtureStateVerbStartStateAny(up);

    def undefined: FixtureStateVerbStartStateUndefined
          = new FixtureStateVerbStartStateUndefined(up);
  }
  
  class FixtureStateVerbStartState(up:DSLExpression,x:T#StartStateType) 
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
  {
    def value = up.value.withStartState(x);
  }

  class FixtureStateVerbStartStates(up:DSLExpression,x:Seq[T#StartStateType]) 
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
  {
    def value = up.value.withStartStates(x);
  }

  class FixtureStateVerbStartStateAny(up:DSLExpression)
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
  {
    def value = up.value.withAnyState;
  }

  class FixtureStateVerbStartStateUndefined(up:DSLExpression)
                                                        extends DSLExpression
  {
    def value = up.value.withUndefinedState;
  }

  trait FixtureStateVerb_Aspects extends DSLExpression
  {
    def aspects(x: T#StateAspectType *): DSLExpression =
    {
     throw new RuntimeException("Not implemented");
    }
  }

}

// vim: set ts=4 sw=4 et:
