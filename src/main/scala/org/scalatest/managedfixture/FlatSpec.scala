package org.scalatest.managedfixture

import org.scalatest._
import org.scalatest.fixture.NoArgTestWrapper
import verb.{ResultOfTaggedAsInvocation, ResultOfStringPassedToVerb, BehaveWord, ShouldVerb, MustVerb, CanVerb}
import scala.collection.immutable.ListSet
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import scala.util.DynamicVariable

import ua.gradsoft.managedfixture._

private[scalatest] class InternalFlatSpec[T <: FixtureStateTypes](val owner:FlatSpec[T]) extends fixture.FlatSpec
                                                  with AbstractManagedFixtureStateSuite[T]
{

  def this() =
    this( FlatSpecConstructorKluge.currentOwner.value.get.asInstanceOf[FlatSpec[T]] )

  def fixtureStateTypes = owner.fixtureStateTypes;
  def fixtureAccess = owner.fixtureAccess;

  var currentBranchName: Option[String] = None;

  def behavior_of(description:String)={
    currentBranchName = Some(description);
    behavior.of(description);
  }

  def setFixtureStateForTest(verb: String, rest:String, tags: List[Tag], testFun: FixtureParam=>Any ) =
  {
    val testName = fullTestName(verb+" "+rest);
    neededFixtureStates(testName) = fixtureStateForNextTest.getOrElse(defaultFixtureState);
    val nestedTestSuite = createNestedInstanceForTest(testName);
    // need to register test in nested suite (since this verbs not in out constructor)
    currentBranchName.foreach(
       nestedTestSuite.behavior.of(_)
    );
    nestedTestSuite.it_verbStringTaggedAs_in(verb,rest,tags,testFun);
    suitesToRun(testName) = nestedTestSuite;
  }


                     // get test of name for barnah part.
                     // in general -- this is depend from resources in ScalatestResourceBundle
                     //  and must be called via scalatest API which not exists yet.
  private[scalatest] def fullTestName(text:String) = currentBranchName.getOrElse("")+" "+text;

  // exoressions in FlatSpec API

  def it_verbStringTaggedAs_in(verb: String, name: String ,tags: List[Tag], testFun: FixtureParam => Any): Unit =
  {
   if (!isNested) {
     setFixtureStateForTest(verb, name, tags, testFun)
   } else {
     new ItVerbStringTaggedAs(verb, name, tags).in(testFun);
   }
  }

  def it_verbStringTaggedAs_is(verb: String, name: String ,tags: List[Tag], testFun: =>PendingNothing ): Unit =
  {
    new ItVerbStringTaggedAs(verb, name, tags).is(testFun);
  }

  def it_verbStringTaggedAs_ignore(verb: String, name: String ,tags: List[Tag], testFun: FixtureParam => Any): Unit =
  {
    new ItVerbStringTaggedAs(verb, name, tags).ignore(testFun);
  }

  def it_verbStringTaggedAs_ignore(verb: String, name: String ,tags: List[Tag], testFun: () => Any): Unit =
  {
    new ItVerbStringTaggedAs(verb, name, tags).ignore(testFun);
  }
  

  def ignore_verbStringTaggedAs_in(verb: String, name: String, tags: List[Tag], testFun: FixtureParam => Any) =
             new IgnoreVerbStringTaggedAs(verb, name, tags).in(testFun)

  def ignore_verbStringTaggedAs_is(verb: String, name: String, tags: List[Tag], testFun: => PendingNothing) =
             new IgnoreVerbStringTaggedAs(verb, name, tags).is(testFun)


  def inAndIgnoreMethods_in(resultOfStringPassedToVerb: ResultOfStringPassedToVerb, testFun: FixtureParam => Any) =
  {
    import resultOfStringPassedToVerb._
    it_verbStringTaggedAs_in(verb,rest,List(),testFun)
  }

  def inAndIgnoreMethods_ignore(resultOfStringPassedToVerb: ResultOfStringPassedToVerb, testFun: FixtureParam => Any) =
  {
    import resultOfStringPassedToVerb._
    it_verbStringTaggedAs_ignore(verb,rest,List(),testFun)
  }

  def inAndIgnoreMethodsAfterTaggedAs_in(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation, testFun: FixtureParam => Any) =
  {
    import resultOfTaggedAsInvocation.verb
    import resultOfTaggedAsInvocation.rest
    import resultOfTaggedAsInvocation.{tags=>tagsList}
    it_verbStringTaggedAs_in(verb,rest,tagsList,testFun)
  }

  def inAndIgnoreMethodsAfterTaggedAs_ignore(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation, 
                                             testFun: FixtureParam => Any) =
  {
    import resultOfTaggedAsInvocation.verb
    import resultOfTaggedAsInvocation.rest
    import resultOfTaggedAsInvocation.{tags=>tagsList}
    it_verbStringTaggedAs_in(verb,rest,tagsList,testFun)
  }

  // access to private methods:
  def _info = info;


  def _fixtureUsage(usage: TestFixtureStateUsageDescription[T])
  {  fixtureUsage(usage); }


  def _fixtureUsage(dsl: DSLExpression)
  {  fixtureUsage(dsl.value); }

  //
  override def createNestedInstanceForTest(testName: String) =
  {
    FlatSpecConstructorKluge.currentOwner.withValue(Some(owner)){
        super.createNestedInstanceForTest(testName)
    }.asInstanceOf[InternalFlatSpec[T]];
  }

/*
  // just interceptors to see debug.
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    super.run(testName, reporter, stopper, filter, configMap, distributor, tracker);
  }

  protected override def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any],
                                distributor: Option[Distributor], tracker: Tracker)=
  {
    super.runNestedSuites(reporter, stopper, filter, configMap, distributor, tracker);
  }
*/
  

}

/**
  * export [[org.scalatest.fixture.FlatSpec]] API to world of managed fixtures. 
  *
  *{{{
  * import org.scalatest.managedfixture._
  *
  * class MyFlatSpec extends managedfixture.[DBFixtureStateTypes.type]
  * {
  *  val fuxtureStateTypes = DBFixtureStateTypes
  *  val fuxtureAccess = DBFixtureAccess
  *  import DBFixtureStateTypes.States._
  *
  *  behavior of "My datababase"
  *  
  *  start state(INITIAL) finish state(WITH_USERS)
  *  it should "be able to add user " in {
  *     .....
  *  }
  *
  *  start state(WITH_USERS) change undefined
  *  it should "retrieve user with name Jon in our test dataset" in {
  *     inTransaction { 
  *        val x = db.selectUser("Jon").headOption;      
  *        assert(x!=None)
  *        assert(x.name == "Jon")
  *     }
  *  }
  *
  *
  *}}} 
  */
trait FlatSpec[T <: FixtureStateTypes] extends Suite with ShouldVerb with MustVerb with CanVerb 
                                           with FixtureStateDSL[T]
{ 

  type FixtureStateTypes = T;
  type FixtureParam = T#FixtureType;

  /**
   * Must be defined in subclass.
   **/
  def fixtureAccess: FixtureAccess[T]

  /**
   * must be defined in subclass.
   **/
  def fixtureStateTypes: T

  override protected def fixtureUsageDSLValueAction(value: => TestFixtureStateUsageDescription[T]): Unit =
  {
   internalSpec._fixtureUsage(value);
  }


  // here we recreate internal suite and will be pass to one all 'real' functionality.
  private[scalatest] lazy val internalSpec: InternalFlatSpec[T] = 
                                            if (this.isInstanceOf[Grouped]) {
                                               if (GroupSpecConstructorKluge.currentOwner!=None) {
                                                 GroupSpecConstructorKluge.currentOwner.value.get.asInstanceOf[FlatSpecGroup[T]].internalSpec;
                                               } else {
                                                  // it was called outside group, create internal constructor
                                                  new InternalFlatSpec(this);
                                               }
                                             } else {
                                               new InternalFlatSpec(this); 
                                             }

  implicit protected def info: Informer = internalSpec._info;

  protected final class BehaviorWord {
    def of(description: String) {
       internalSpec.behavior_of(description);
    }
  }
  protected val behavior = new BehaviorWord

  protected final class ItVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {
    def in(testFun: () => Any) {
      internalSpec.it_verbStringTaggedAs_in(verb,name,tags,new NoArgTestWrapper(testFun))
    }
    def in(testFun: FixtureParam => Any) {
      internalSpec.it_verbStringTaggedAs_in(verb,name,tags,testFun)
    }
    def is(testFun: => PendingNothing) {
      internalSpec.it_verbStringTaggedAs_is(verb,name,tags,testFun)
    }
    def ignore(testFun: () => Any) {
      internalSpec.it_verbStringTaggedAs_ignore(verb,name,tags,testFun)
    }
    def ignore(testFun: FixtureParam => Any) {
      internalSpec.it_verbStringTaggedAs_ignore(verb,name,tags,testFun)
    }
  }

  protected final class ItVerbString(verb: String, name: String) {
    def in(testFun: () => Any) {
      internalSpec.it_verbStringTaggedAs_in(verb,name,List(),new NoArgTestWrapper(testFun))
    }
    def in(testFun: FixtureParam => Any) {
      internalSpec.it_verbStringTaggedAs_in(verb,name,List(),testFun)
    }
    def is(testFun: => PendingNothing) {
      internalSpec.it_verbStringTaggedAs_is(verb,name,List(),testFun)
    }
    def ignore(testFun: () => Any) {
      internalSpec.it_verbStringTaggedAs_ignore(verb,name,List(),testFun)
    }
    def ignore(testFun: FixtureParam => Any) {
      internalSpec.it_verbStringTaggedAs_ignore(verb,name,List(),testFun)
    }
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  protected final class ItWord {
    def should(string: String) = new ItVerbString("should", string)
    def must(string: String) = new ItVerbString("must", string)
    def can(string: String) = new ItVerbString("can", string)
    def should(behaveWord: BehaveWord) = behaveWord
    def must(behaveWord: BehaveWord) = behaveWord
    def can(behaveWord: BehaveWord) = behaveWord
  }
  protected val it = new ItWord

  protected final class IgnoreVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {
    def in(testFun: () => Any) {
      internalSpec.ignore_verbStringTaggedAs_in(verb, name, tags, new NoArgTestWrapper(testFun));
    }
    def in(testFun: FixtureParam => Any) {
      internalSpec.ignore_verbStringTaggedAs_in(verb, name, tags, testFun);
    }
    def is(testFun: => PendingNothing) {
      internalSpec.ignore_verbStringTaggedAs_is(verb, name, tags, testFun);
    }
  }

  protected final class IgnoreVerbString(verb: String, name: String) {
    def in(testFun: () => Any) {
      internalSpec.ignore_verbStringTaggedAs_in(verb, name, List(), new NoArgTestWrapper(testFun));
    }
    def in(testFun: FixtureParam => Any) {
      internalSpec.ignore_verbStringTaggedAs_in(verb, name, List(), testFun);
    }
    def is(testFun: => PendingNothing) {
      internalSpec.ignore_verbStringTaggedAs_is(verb, name, List(), testFun);
    }
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new IgnoreVerbStringTaggedAs(verb, name, tagList)
    }
  }

  protected final class IgnoreWord {
    def should(string: String) = new IgnoreVerbString("should", string);
    def must(string: String) = new IgnoreVerbString("must", string)
    def can(string: String) = new IgnoreVerbString("can", string)
  }

  protected val ignore = new IgnoreWord

  protected final class InAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) {
    def in(testFun: () => Any) {
      internalSpec.inAndIgnoreMethods_in(resultOfStringPassedToVerb, new NoArgTestWrapper(testFun));
    }

    def ignore(testFun: () => Any) {
      internalSpec.inAndIgnoreMethods_ignore(resultOfStringPassedToVerb,new NoArgTestWrapper(testFun));
    }

    def in(testFun: FixtureParam => Any) {
      internalSpec.inAndIgnoreMethods_in(resultOfStringPassedToVerb,testFun);
      //registerTestToRun(verb + " " + rest, "in", List(), testFun)
    }

    def ignore(testFun: FixtureParam => Any) {
      internalSpec.inAndIgnoreMethods_ignore(resultOfStringPassedToVerb,testFun);
      //registerTestToIgnore(verb + " " + rest, "ignore", List(), testFun)
    }
  }

  protected implicit def convertToInAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) =
    new InAndIgnoreMethods(resultOfStringPassedToVerb)

  protected final class InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) {

    def in(testFun: () => Any) {
      internalSpec.inAndIgnoreMethodsAfterTaggedAs_in(resultOfTaggedAsInvocation, new NoArgTestWrapper(testFun));
      //registerTestToRun(verb + " " + rest, "in", tagsList, new NoArgTestWrapper(testFun))
    }

    def ignore(testFun: () => Any) {
      internalSpec.inAndIgnoreMethodsAfterTaggedAs_ignore(resultOfTaggedAsInvocation, new NoArgTestWrapper(testFun));
      //registerTestToIgnore(verb + " " + rest, "ignore", tagsList, new NoArgTestWrapper(testFun))
    }

    def in(testFun: FixtureParam => Any) {
      internalSpec.inAndIgnoreMethodsAfterTaggedAs_in(resultOfTaggedAsInvocation, testFun);
      //registerTestToRun(verb + " " + rest, "in", tagsList, testFun)
    }

    def ignore(testFun: FixtureParam => Any) {
      internalSpec.inAndIgnoreMethodsAfterTaggedAs_ignore(resultOfTaggedAsInvocation, testFun);
      //registerTestToIgnore(verb + " " + rest, "ignore", tagsList, testFun)
    }
  }

  protected implicit def convertToInAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) =
    new InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation)

  protected implicit val shorthandTestRegistrationFunction: (String, String, String) => ResultOfStringPassedToVerb = {
    (subject, verb, rest) => {
      behavior.of(subject)
      new ResultOfStringPassedToVerb(verb, rest) {
        def is(testFun: => PendingNothing) {
          internalSpec.it_verbStringTaggedAs_is(verb, rest, List(),testFun);
          //registerTestToRun(verb + " " + rest, "is", List(), unusedFixtureParam => testFun)
        }
        def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
          val tagList = firstTestTag :: otherTestTags.toList
          new ResultOfTaggedAsInvocation(verb, rest, tagList) {
            // "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
            //                                                            ^
            def is(testFun: => PendingNothing) {
              internalSpec.it_verbStringTaggedAs_is(verb, rest, tags,testFun);
              //registerTestToRun(verb + " " + rest, "is", tags, new NoArgTestWrapper(testFun _))
            }
          }
        }
      }
    }
  }

  protected implicit val shorthandSharedTestRegistrationFunction: (String) => BehaveWord = {
    (left) => {
      behavior.of(left)
      new BehaveWord
    }
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    if (isGrouped) {
       this.asInstanceOf[Grouped].checkGroupExists( classOf[FlatSpecGroup[_]] );
    }
    internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker);
  }

  protected val behave = new BehaveWord
  
  private def isGrouped: Boolean = this.isInstanceOf[Grouped];
}

private[scalatest] object FlatSpecConstructorKluge
{

  val currentOwner = new DynamicVariable[Option[FlatSpec[_]]](None);

}
