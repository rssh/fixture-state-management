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
class FunSuite[Fixture,State](group:GroupSuite, 
                              fixture: Option[Fixture],
                              testToRun:Option[String]) extends fixture.FunSuite
                                                          with FixtureStateDSL[State] 
{

  type FixtureParam = Fixture

  private var currentFixture: Option[Fixture] = fixture ;

  override def withFixture(test: OneArgTest) =
  {
    if (testToRun.isEmpty) {
       throw new IllegalStateException("Attempt to call withFixture with empty testToRun");
    }
    if (currentFixture.isEmpty) {
       throw new IllegalStateException("Attempt to call withFixture with empty fuxture");
    }
    test(currentFixture.get)
  }


  protected override def fixtureUsageDSLValueAction(value: => FixtureStateUsageDescription[State]): Unit =
  { 
    System.err.println("action for "+value);
    usage = Some(value)
  }

  private def isAnalyzePass = testToRun.isEmpty

  var usage: Option[FixtureStateUsageDescription[State]] = None
 
  override def test(name:String, tags:Tag*)(f:FixtureParam=>Any):Unit =
  {
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
       val fa = FixtureAccessOperation[Unit,Fixture,State](
                    (fixture => {
                                  val runCopy = createCopy(group,Some(fixture),Some(name))
                                  runCopy.run(Some(name),???)
                                }),u )
       usage = None
   }

   def createCopy(g:GroupSuite, f:Option[Fixture], testToRun:Option[String]): FunSuite[Fixture,State] =
   {
    import scala.reflect.runtime.{universe => ru}
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val classSymbol = mirror.classSymbol(getClass)
    val classMirror = mirror.reflectClass(classSymbol)
    val constructorMethod = classSymbol.typeSignature.member(ru.termNames.CONSTRUCTOR)  match {
       case ru.NoSymbol =>
             throw new IllegalStateException(s"class ${classSymbol} must have constructor");
       case cn => 
              if (cn.isMethod) {
                 cn.asMethod
              } else {
                throw new IllegalStateException(s"class ${classSymbol} must have only one constructor");
              }
    }
    val constructorMirror = classMirror.reflectConstructor(constructorMethod)
    val instance = constructorMirror.apply(g,f,testToRun)
    instance.asInstanceOf[FunSuite[Fixture,State]]
   }

}
