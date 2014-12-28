package ua.gradsoft.managedfixture

/**
 * DLS for fixture state: sentence in this DSL describe: how some test is related to fixture,
 * i.e. it expect that fixture must be in some concerete state at the end leave state unchanged
 * after test execution, or may be execution invalidate current fixture state and so on.
 *
 * Grammar:
 * {{{
 * TestUsageDescription:  Mark ([StartDescription] | 
 *                              [FinishDescription] | 
 *                              [ChangeDescription] |
 *                              [ExecutionDescription] )*
 *
 * StartDescription: start(StartStateDescription) [aspects (all | '(' {ID}* ')' | none) ]
 * FinishDescription: finish(FinishStateDescription)
 * CnangesDescription: change [nothing| any] 
 *
 * StartStateDescription: state (any | undefined | {ID} )
 *                       |
 *                        states({ID}[,{ID}]*)
 *
 * FinishStateDescription: state (undefined | {ID} )
 *
 * ExecutionDescription: execution (sequential | parallel)
 * }}}
 * 
 * Examples of such expession
 * {{{
 *   start state(any)
 *   start state(any) change(noting)
 *   start state(any) finish state(undefined)
 *   start state(S3) finish state(S4)
 *   start states(S1,S2,S3) aspects (1,2,3) change(nothing)
 *   start state(S3) change(nothing) execution(parallel)
 * }}}
 *
 **/
trait FixtureStateDSL[T <: FixtureStateTypes]
{

  def fixtureStateTypes: T

  /**
   * optional action which can be used by client, overriding this method to
   * perform some custom steps after receiving new value in this DSL.
   * By default- do nothing and return given value.
   **/
  protected def fixtureUsageDSLAction[A <:DSLExpression](dsl: A):A =
  {
   fixtureUsageDSLValueAction(dsl.value);
   dsl
  }

  /**
   * called when we receive new value. (by default - nothing).
   **/ 
  protected def fixtureUsageDSLValueAction(value: => FixtureStateUsageDescription[T]): Unit =
  {
  }


  trait DSLExpression
  {
    def value: FixtureStateUsageDescription[T]
    def string: String
  }

  class FixtureStateVerb extends DSLExpression
                        with FixtureStateVerb_Start
  { 
    val value = FixtureStateUsageDescription[T](fixtureStateTypes);
    val string = "FixtureStateVerb";
  }
                          

  trait FixtureStateVerb_Start extends DSLExpression
  {

    def start(x: FixtureStateVerb_STATE0.type):FixtureStateVerbStartState0 =
          new FixtureStateVerbStartState0(this);

    def start(x: FixtureStateVerb_STATE_ANY.type):FixtureStateVerbStartStateAny =
          fixtureUsageDSLAction(new FixtureStateVerbStartStateAny(this));

    def start(x: FixtureStateVerb_STATE_UNDEFINED.type):FixtureStateVerbStartStateUndefined =
          fixtureUsageDSLAction(new FixtureStateVerbStartStateUndefined(this));

    def start(x: FixtureStateVerb_STATE): FixtureStateVerbStartState =
          fixtureUsageDSLAction(new FixtureStateVerbStartState(this,x.s));

    def start(x: FixtureStateVerb_STATES) : FixtureStateVerbStartStates = 
          fixtureUsageDSLAction(new  FixtureStateVerbStartStates(this,x.args));

  }

    
  case class FixtureStateVerb_STATE(val s:T#State);
  case object FixtureStateVerb_STATE0;
  case object FixtureStateVerb_ANY;
  case object FixtureStateVerb_UNDEFINED;
  case object FixtureStateVerb_STATE_ANY;
  case object FixtureStateVerb_STATE_UNDEFINED;
  case class FixtureStateVerb_STATES(val args:Seq[T#State]);

  val any = FixtureStateVerb_ANY;
  val undefined = FixtureStateVerb_UNDEFINED;
                
  def state(x:T#State) = FixtureStateVerb_STATE(x);
  val state = FixtureStateVerb_STATE0;
  def states(x:T#State*) = FixtureStateVerb_STATES(x);

  def state(x:FixtureStateVerb_ANY.type) = FixtureStateVerb_STATE_ANY;
  def state(x:FixtureStateVerb_UNDEFINED.type) = FixtureStateVerb_STATE_UNDEFINED;

  // also let-s expand start into top-level
  class FixtureStateVerbStart extends DSLExpression
  {
    val value = FixtureStateUsageDescription[T](fixtureStateTypes);

    def state(x: FixtureStateVerb_ANY.type) =
            fixtureUsageDSLAction(new FixtureStateVerbStartStateAny(this));

    def state(x: FixtureStateVerb_UNDEFINED.type) =
            fixtureUsageDSLAction(new FixtureStateVerbStartStateUndefined(this));

    def state(x:T#State) = 
            fixtureUsageDSLAction(new FixtureStateVerbStartState(this,x));

    def states(x:T#State*) = 
            fixtureUsageDSLAction(new  FixtureStateVerbStartStates(this,x));

    val string = "start ";
  }
 
  def start = new FixtureStateVerbStart

  class FixtureStateVerbStartState0(up: DSLExpression)
  {
    def any: FixtureStateVerbStartStateAny
          = fixtureUsageDSLAction(new FixtureStateVerbStartStateAny(up));

    def undefined: FixtureStateVerbStartStateUndefined
          = fixtureUsageDSLAction(new FixtureStateVerbStartStateUndefined(up));
  }
  
  class FixtureStateVerbStartState(up:DSLExpression,x:T#State) 
                                                        extends DSLExpression
                                                        with FixtureStateVerb_Aspects
                                                        with FixtureStateVerb_Finish
                                                        with FixtureStateVerb_Change
  {
    def value = up.value.withStartState(x);
    def string = "start state("+x.toString+")";
  }

  class FixtureStateVerbStartStates(up:DSLExpression,x:Seq[T#State]) 
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
    def aspects(x: T#Aspect *): FixtureStateVerbAspects =
    {
     fixtureUsageDSLAction(new FixtureStateVerbAspects(this,x));
    }
  }

  class FixtureStateVerbAspects(up:DSLExpression, x:Seq[T#Aspect]) 
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
          fixtureUsageDSLAction(new FixtureStateVerbFinishState(this,x.s))

    def finish(x: FixtureStateVerb_STATE_UNDEFINED.type):FixtureStateVerbFinishStateUndefined =
          fixtureUsageDSLAction(new FixtureStateVerbFinishStateUndefined(this))
  }

  class FixtureStateVerbFinishState(up:DSLExpression, x:T#State) extends DSLExpression
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
         = fixtureUsageDSLAction( new FixtureStateVerbChangeNothing(this) );
    //def change(x: FixtureStateVerb_ANY.type)
  }

  class FixtureStateVerbChangeNothing(up:DSLExpression) extends DSLExpression
  {
    def value = up.value.withChangeNothing;
    def string = up.string+"  change(nothing)";
    def parallel = new FixtureStateVerbChangeNothingParallel(this);
    def execution(v: FixtureStateVerb_ExecutionSpec) = 
            new FixtureStateVerbExecution(this,v);
  }

  class FixtureStateVerbChangeNothingParallel(up:DSLExpression) extends DSLExpression
  {
    def value = up.value.withParallel(true);
    def string = up.string+"  parallel";
  }


  case object FixtureStateVerb_NOTHING

  def nothing = FixtureStateVerb_NOTHING

  class FixtureStateVerbExecution(up:DSLExpression, 
                                  spec: FixtureStateVerb_ExecutionSpec) 
                                                         extends DSLExpression
  {
    def value = up.value.withParallel(spec.canRunParallel)
    def string = up.string+" execution("+spec.string+")"
  }

  sealed trait FixtureStateVerb_ExecutionSpec
  {  
     def canRunParallel: Boolean
     def string:String 
  }

  case object FixtureStateVerb_PARALLEL extends FixtureStateVerb_ExecutionSpec
  {  
     def canRunParallel = true;
     def string="parallel"; 
  }

  case object FixtureStateVerb_SEQUENTIAL extends FixtureStateVerb_ExecutionSpec
  {  
     def canRunParallel = false;
     def string="sequential"; 
  }
 

  def parallel = FixtureStateVerb_PARALLEL;
  def sequential = FixtureStateVerb_SEQUENTIAL;


}

// vim: set ts=4 sw=4 et:
