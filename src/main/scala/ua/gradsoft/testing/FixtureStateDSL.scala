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
 *   `fixture state` start states(S1,S2,S3) aspects (1,2,3) change(none)
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
    def string: String
  }


  class FixtureStateVerb extends DSLExpression
                        with FixtureStateVerb_Start
  { 
    val value = TestFixtureStateUsageDescription[T](fixtureStateTypes);
    val string = "FixtureStateVerb";
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

  // also let-s expand start into top-level
  class FixtureStateVerbStart extends DSLExpression
  {
    val value = TestFixtureStateUsageDescription[T](fixtureStateTypes);

    def state(x: FixtureStateVerb_ANY.type) =
            new FixtureStateVerbStartStateAny(this);

    def state(x: FixtureStateVerb_UNDEFINED.type) =
            new FixtureStateVerbStartStateUndefined(this);

    def state(x:T#StartStateType) = 
            new FixtureStateVerbStartState(this,x);

    def states(x:T#StartStateType*) = 
            new  FixtureStateVerbStartStates(this,x);

    val string = "start ";
  }
 
  def start = new FixtureStateVerbStart

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
                                                        with FixtureStateVerb_Finish
                                                        with FixtureStateVerb_Change
  {
    def value = up.value.withStartState(x);
    def string = "start state("+x.toString+")";
  }

  class FixtureStateVerbStartStates(up:DSLExpression,x:Seq[T#StartStateType]) 
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
                                                        with FixtureStateVerb_Finish
                                                        with FixtureStateVerb_Change
  {
    def value = up.value.withStartStates(x);
    def string = "start states("+x.mkString(",")+")";
  }

  class FixtureStateVerbStartStateAny(up:DSLExpression)
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
                                                        with FixtureStateVerb_Finish
                                                        with FixtureStateVerb_Change
  {
    def value = up.value.withAnyState;
    def string = "start state(any)";
  }

  class FixtureStateVerbStartStateUndefined(up:DSLExpression)
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Finish
                                                        with FixtureStateVerb_Change
  {
    def value = up.value.withUndefinedState;
    def string = "start state(undefined)";
  }

  trait FixtureStateVerb_Aspects extends DSLExpression
  {
    def aspects(x: T#StateAspectType *): FixtureStateVerbAspects =
    {
     new FixtureStateVerbAspects(this,x);
    }
  }

  class FixtureStateVerbAspects(up:DSLExpression, x:Seq[T#StateAspectType]) 
                                                         extends DSLExpression
                                                         with FixtureStateVerb_Finish
                                                         with FixtureStateVerb_Change
  {
    def value = up.value.withStateAspects(x);
    def string = up.string+" aspects("+x.mkString(",")+")";
  }

  trait FixtureStateVerb_Finish extends DSLExpression
  {
    def finish(x: FixtureStateVerb_STATE): FixtureStateVerbFinishState =
          new FixtureStateVerbFinishState(this,x.s)

    def finish(x: FixtureStateVerb_STATE_UNDEFINED.type):FixtureStateVerbFinishStateUndefined =
          new FixtureStateVerbFinishStateUndefined(this)
  }

  class FixtureStateVerbFinishState(up:DSLExpression, x:T#StartStateType) extends DSLExpression
  {
    def value = up.value.withFinishState(x);
    def string = up.string+"  finish state("+x+")";
  }

  class FixtureStateVerbFinishStateUndefined(up:DSLExpression) extends DSLExpression
  {
    def value = up.value.withFinishStateUndefined;
    def string = up.string+"  finish state(undefined)";
  }

  trait FixtureStateVerb_Change extends DSLExpression
  {
    def change(x: FixtureStateVerb_NOTHING.type)
         = new FixtureStateVerbChange(this);
  }

  class FixtureStateVerbChange(up:DSLExpression) extends DSLExpression
  {
   def value = up.value.withChangeNothing;
    def string = up.string+"  change(nothing)";
  }

  case object FixtureStateVerb_NOTHING

  def nothing = FixtureStateVerb_NOTHING

}

// vim: set ts=4 sw=4 et:
