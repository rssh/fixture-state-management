package ua.gradsoft.scalatest.state.internal

import org.scalatest._
import ua.gradsoft.managedfixture._

import scala.collection.mutable.{Map => MutableMap};
import scala.collection.mutable.LinkedHashMap;
import scala.util._;


class Base1S3Test extends fixture.FunSuite
                             with FixtureStateDSL[Base1FixtureStateInfo.type]
                             with SequentialNestedSuiteExecution
{


  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;

  type FST = Base1FixtureStateInfo.type;
  type FixtureParam = FST#FixtureType;

  def  stateManager: FixtureStateManager[FST] = 
    if (isNested) {
       _parent.get.stateManager
    } else {
       _stateManager
    }

  lazy val _stateManager = new FixtureStateManager[FST](fixtureAccess);
  lazy val dummyStateData = TestFixtureStateUsageDescription[FST](fixtureStateTypes).withAnyState;

  var currentFixtureData = TestFixtureStateUsageDescription[FST](fixtureStateTypes);

  def testStateUsageDescriptions: MutableMap[String, TestFixtureStateUsageDescription[FST]] =
      if (isNested)
        _parent.get.testStateUsageDescriptions
      else
        _testStateUsageDescriptions;

  lazy val _testStateUsageDescriptions : MutableMap[String, TestFixtureStateUsageDescription[FST]] = LinkedHashMap();

  private[Base1S3Test] def isNested : Boolean = (_parent != None);

  private[Base1S3Test] var _parent : Option[Base1S3Test] = {
           Base1S3TestConstructorKluge.currentParent.value map { x =>
                Base1S3TestConstructorKluge.currentParent.value=None;
                x;
           }
  }

  private[Base1S3Test] var _parentTestName : Option[String] = {
           Base1S3TestConstructorKluge.currentTestName.value map { x =>
              Base1S3TestConstructorKluge.currentTestName.value=None;
              x;
           }
  }

  private def createNestedInstanceForTest(testName:String) = {
    Base1S3TestConstructorKluge.currentParent.withValue(Some(this)){
      Base1S3TestConstructorKluge.currentTestName.withValue(Some(testName)){
         this.getClass.newInstance.asInstanceOf[Base1S3Test];
      }
    }
  }

  def fixtureUsage(x: DSLExpression) =
  {  currentFixtureData = x.value; }

  def withFixture(test: OneArgTest)
  {
    // 
    val x = testStateUsageDescriptions.get(test.name).getOrElse( dummyStateData );
    stateManager.doWith(x,test);
  }


  override def nestedSuites = suitesToRun.values.toList;
  private[this] var suitesToRun: MutableMap[String,Suite] = MutableMap[String,Suite]();

  protected override def test(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    if (!isNested) {
      testStateUsageDescriptions(testName) = currentFixtureData;
      val nestedTestSuite = createNestedInstanceForTest(testName);
      // not needed - will be called during construction.
      //nestedTestSuite.test(testName, testTags: _* )(testFun);
      suitesToRun(testName) = nestedTestSuite;
    } else {
      if (testName == _parentTestName.get) {
        super.test(testName, testTags: _* )(testFun);
      }
    }
  }

  protected override def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any], 
                                distributor: Option[Distributor], tracker: Tracker)=
  {
   if (!isNested) {
    fixtureAccess.suiteLevelLock.map(_.acquire);
    try {
     val sequenceParts = ExecutionSequenceOptimizer.optimizeOrder(testStateUsageDescriptions);
     for(l <- sequenceParts) {
       // must be run without distributor
       for(nested <- l) {
          // TODO: think about stopRequested
          suitesToRun(nested).run(None,reporter,stopper,filter,configMap,distributor,tracker);
       }
     }
    } finally {
      fixtureAccess.suiteLevelLock.map(_.release);
    }
   } else {
     super.runNestedSuites(reporter, stopper, filter, configMap, distributor, tracker);
   }
  }
  
  override def suiteName = if (isNested) super.suiteName + ":" + _parentTestName
                           else super.suiteName ;
  

  import Base1FixtureStateInfo.States._;

  // test, to check that we run all those tst sequentially in some order.

  fixtureUsage(start state(TWO) change(nothing))
  test("withDSL [3]: start state(TWO) change nothing") { x =>
    assert(x==2);
    assert(Base1S3TestMarkObject.x == "afterONE");
    Base1S3TestMarkObject.x = "afterTWO_CN";
  }

  fixtureUsage(start state(ONE) finish state(TWO))
  test("withDSL [3]: start state(ONE) finish state(TWO)") { x =>
    assert(x==1);
    Base1S3TestMarkObject.x = "afterONE";
    fixtureAccess.set(TWO);    
  }

  fixtureUsage(start state(TWO) finish state(THREE))
  test("withDSL [3]: start state(TWO) finish state(THREE)") { x =>
    assert(x==2);
    assert(Base1S3TestMarkObject.x == "afterTWO_CN");
    fixtureAccess.set(THREE);    
  }

}

// used to pass parameter to nested constructor (since we have
// test method, called in initialization, so we can't set nested
// parameters after init: it's too late)
object Base1S3TestConstructorKluge
{

  val currentParent = new DynamicVariable[Option[Base1S3Test]](None);
  val currentTestName = new DynamicVariable[Option[String]](None);

}

object Base1S3TestMarkObject
{
  var x: String = "ini";
}

// vim: set ts=4 sw=4 et:
