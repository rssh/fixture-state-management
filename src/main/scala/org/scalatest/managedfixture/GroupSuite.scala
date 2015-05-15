package org.scalatest.managedfixture

import scala.language.postfixOps
import org.scalatest._
import org.scalatest.events._
import ua.gradsoft.managedfixture._
import scala.collection.immutable.TreeSet
import scala.collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import scala.util._
import org.reflections._

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Suite._

/**
 * GroupSuite[F,S] scans all managedfixture.FunSuite in subpackages
 *  and run ones in groups, where each group is bound to some
 *  specific instance of FixtureAccessBox.
 **/
abstract class GroupSuite[F,S] extends Suite
{

    val xtestTimeout = 1 minute
    val defaultTestsInBox = 5
    def fixtureAccessBoxFactory: FixtureAccessBoxFactory[F] 

    class SequentialGroupPart(testIndexes: IndexedSeq[Int],
                              groupIndex:Int, 
                              groupIndexWidth: Int,
                              fixtureAccessBox: Future[FixtureAccessBox[F]]
                             ) extends Suite with SequentialNestedSuiteExecution
                                             with StopOnFailure
    {

      override lazy val testNames: Set[String] = {
        var retval = TreeSet[String]() 
        val (g,gw) = (groupIndex, groupIndexWidth)
        val iw = log10(testIndexes.length)+1
        testIndexes.zipWithIndex foreach {
          case (k,i) => retval += s"${i2s(g,gw)}:${i2s(i,iw)}:${registeredTests(k).name}"
        }
        retval
      }

      override def tags() = autoTagClassAnnotations(Map(),this)

      override def suiteName() = 
         s"${GroupSuite.this.getClass.getSimpleName}:SeqGroup:${groupIndex}"

      override def runTests(testName:Option[String], args:Args):Status =
        testName match {
          case Some(name) => 
                       val retval = super.runTests(testName,args)
                       retval
          case None =>
                       val retval = super.runTests(testName,args)
                       retval.whenCompleted{ _ =>
                          fixtureAccessBox.foreach{x => 
                                                   x.close()
                                                  }
                       }
                       retval
        }

      val orderRef = new AtomicReference[Promise[Boolean]]
      orderRef.set(Promise successful true)

      override def runTest(testName:String, args:Args):Status =
      {
         val nextOrder = Promise[Boolean]()
         val prevOrder = orderRef.getAndSet(nextOrder)
         val (i,name) = extractNameIndex(testName)
         val test = registeredTests(i)
         val res = for{box <- fixtureAccessBox;
                        ord <- prevOrder.future;
                        r <- box.apply{ f =>
                               test.value.runInCopy(GroupSuite.this,f,name,args)
                             }
                      } yield r
         res.onComplete( _ => nextOrder success true)

        // TODO: make promise, which will cancel test on timeoit.
        new Status {
           override def isCompleted() = res.isCompleted

           override def succeeds() =
            try {
              Await.result(res, xtestTimeout).succeeds()
            } catch {
               case ex: TimeoutException =>
                  //args.reporter(NoteProvided(ordinal,"timeout exception",NameInfo(suiteName,suiteId,Some(SequentialGroupPart.this.getClass.getName),Some(testName)),Some(ex)))
                  false
               case ex: Throwable =>
                  //args.reporter(AlertProvided(ordinal,"timeout exception",NameInfo(suiteName,suiteId,Some(SequentialGroupPart.this.getClass.getName),Some(testName)),Some(ex)))
                  false
            }

           override def waitUntilCompleted(): Unit =
           {
            succeeds()
           }

           override def whenCompleted(f:Boolean => Unit):Unit=
            res.onComplete {
                case Failure(ex) => f(false)              
                case Success(s) => s.whenCompleted(f)
            }
           
        }
      }

      def extractNameIndex(packed:String):(Int,String) =
      {
        val parts = packed.split(":").toSeq
        if (parts.size < 3) {
           throw new IllegalArgumentException("Invalid format for testName: must be {g}:{i}:{name} we have"+packed);
        }
        val h1 = parts.head
        val t1 = parts.tail
        val h2 = t1.head
        val t2 = t1.tail
        val name = t2.mkString(":")
        (Integer.parseInt(h2), name)
      }

    }

    override lazy val nestedSuites: scala.collection.immutable.IndexedSeq[org.scalatest.Suite] =
    {
      val tests = findInheritedFromInThisPackage[managedfixture.FunSuite[F,S]](classOf[managedfixture.FunSuite[F,S]])
      tests.foreach( createTestInstance(_) ) // will call register
      if (tests.length==0) {
        scala.collection.immutable.IndexedSeq[Suite]() 
      } else {
        val bf = fixtureAccessBoxFactory
        val nAvailableBoxes = bf.nBoxes.getOrElse( (tests.length-1)/defaultTestsInBox + 1)
        // TODO: split on number of parallelism .
        val st = new StateTransitions(registeredTests)
        val indexes = ExecutionSequenceOptimizer(st,nAvailableBoxes)
        val nBoxes = if (indexes.length < nAvailableBoxes)
                        indexes.length
                     else
                        nAvailableBoxes
        val parts = (0 until indexes.length) map {i=> 
                            new SequentialGroupPart(
                                  indexes(i),i,log10(nBoxes)+1,
                                  bf.box())
                    } 
        parts
      }
    }

    case class RegisteredTest(
                      override val usage: FixtureStateUsageDescription[S],
                      override val value: FunSuite[F,S],
                               val name: String
                             )  extends IndexedByFixtureUsage[FunSuite[F,S],S]

    def register(name: String, 
                 usage: FixtureStateUsageDescription[S],
                 value: FunSuite[F,S]) =
    {
      registeredTests :+= RegisteredTest(usage,value,name) 
    }

    var registeredTests: IndexedSeq[RegisteredTest] = IndexedSeq()

    private def i2s(x:Int, maxLen: Int) =
     x.toString + "0"*(maxLen - log10(x)-1)

    private def log10(x:Int) =
      if      (x >= 1000000000) 9 
      else if (x >= 100000000) 8 
      else if (x >= 10000000) 7  
      else if (x >= 1000000) 6 
      else if (x >= 100000) 5 
      else if (x >= 10000) 4  
      else if (x >= 1000) 3 
      else if (x >= 100) 2 
      else if (x >= 10) 1 
      else 0


   def findInheritedFromInThisPackage[T](tClass: Class[T]):Seq[Class[_ <: T]]
    = reflections.getSubTypesOf(tClass).toSeq

   def createTestInstance[X <: FunSuite[F,S]](f:Class[X]):X =
    ReflectUtil.constructor3(f,this,None,None)

   private lazy val reflections = new Reflections(this.getClass.getPackage)

}

