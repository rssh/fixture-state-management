package ua.gradsoft.managedfixture

import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._
import org.scalatest._

class FutureStatus(fs:Future[Status], maxTimeout: Duration = 1 minute)(implicit ec: ExecutionContext) extends Status
{

 def isCompleted: Boolean = 
 {
  fs value match {
    case None => false
    case Some(tryStatus) => tryStatus match {
                              case Success(status) => status.isCompleted
                              case Failure(ex) => true
                            }
  }
 }

 def succeeds(): Boolean = 
 {
   waitForStatus.succeeds()
 }
 

 def waitUntilCompleted(): Unit = 
 {
   waitForStatus.waitUntilCompleted()
 }

 def whenCompleted(f: Boolean => Unit): Unit = 
 {
   fs.onComplete{
     case Success(status) => status.whenCompleted(f)
     case Failure(e) => f(false)
   }
 }

 private def waitForStatus: Status =
 {
   Await.result(fs, maxTimeout)
 }

}
