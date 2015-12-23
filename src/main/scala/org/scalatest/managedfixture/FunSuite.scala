package org.scalatest.managedfixture

import org.scalatest._
import ua.gradsoft.managedfixture._

/**
 * Special suite
 *
 * Note, that each suite instantiated more then once:
 * - when GroupSuite instantiate each FunSuite to analyze fixture-state-usave
 *   before building sequence of executions. In such case testToRun is empty.
 * - when GroupSuite actually runnign those tests.
 **/
class FunSuite[Fixture,State](group:GroupSuite[Fixture,State], 
                              fixture: Option[Fixture],
                              testToRun:Option[String]) extends fixture.FunSuite
                                                          with FixtureStateDSL[State] 
{

  type FixtureParam = Fixture

  override def withFixture(test: OneArgTest) =
  {
    if (testToRun.isEmpty) {
       throw new IllegalStateException("Attempt to call withFixture with empty testToRun");
    }
    if (fixture.isEmpty) {
       throw new IllegalStateException("Attempt to call withFixture with empty fuxture");
    }
    test(fixture.get)
  }


  protected override def fixtureUsageDSLValueAction(value: => FixtureStateUsageDescription[State]): Unit =
  { 
    usage = Some(value)
  }

  private def isAnalyzePass = testToRun.isEmpty

  var usage: Option[FixtureStateUsageDescription[State]] = None
 
  override def test(name:String, tags:Tag*)(f:FixtureParam=>Any):Unit =
  {
   val threadId = Thread.currentThread.getId
   System.err.println(s"!!! managedFixture::FunSuite.test name = ${name}, testToRun=${testToRun} this=${this} threadId=${threadId}")
   testToRun match {
       case None => testAnalyze(name,tags:_*)(f)
       case Some(testName) =>
               if (testName == name)
                    super.test(name,tags:_*)(f)
   }
  }


  def testAnalyze(name:String, tags:Tag*)(f:Fixture=>Any):Unit =
   usage match {
     case None =>
       throw new IllegalStateException("test without usage specification");
     case Some(u) =>
       group.register(name,u,this)
       usage = None
   }

   def createCopy(g:GroupSuite[Fixture,State], f:Option[Fixture], testToRun:Option[String]): FunSuite[Fixture,State] =
    ReflectUtil.constructor3(getClass,g,f,testToRun)

   def runInCopy(g:GroupSuite[Fixture,State],f:Fixture,name:String, args:Args):Status =
   {
     System.err.println("!!!: managedFixture.FunSuite::runInCopy for "+g.suiteName+" test "+name+" thread "+Thread.currentThread.getId()+" this= "+this)
     try {
       createCopy(g,Some(f),Some(name)).runTests(Some(name),args)
     } catch {
       case ex: Exception =>
                  System.err.println("exception catched, test name = "+name+", testToRun:"+testToRun+", registedTests in g:"+g.registeredTests.map(_.name))
                  throw ex
     }
   }



}
