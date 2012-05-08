package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import org.scalatest._

/**
 * define DSL for execution of tests.
 * 
 * execution grouped    //(default) - mean that 
 */
trait Grouped
{

 
  class ExecutionVerb {
    def grouped: Unit = {
      _grouped = true;
    }
    
    def autonomic: Unit = {
      _grouped = false;
    }  
    
  }
  
  /**
   * 
   */
  protected def execution = new ExecutionVerb
  
  
  //protected var specGroup: SpecGroup;  
  
  /**
   * if is set to true, than executed
   */
  protected var _grouped: Boolean = true;
  protected var _groupedInitialized = false;
  
  protected final def isGrouped = _grouped;
  
  /**
   * called by owner after instantiation
   */
  def mark(owner:SpecGroup):Unit = {}
 
  
  // here we recreate internal suite and will be pass to one all 'real' functionality.
  protected def createInternalSpec[A,B](whenGrouped: A=>B, whenNotGrouped: => B):B = 
                                            if (isGrouped) {
                                               if (GroupSpecConstructorKluge.currentOwner.value!=None) {
                                                 whenGrouped(GroupSpecConstructorKluge.currentOwner.value.get.asInstanceOf[A]);
                                               } else {
                                                  // it was called outside group, create internal constructor
                                                  whenNotGrouped;
                                               }
                                             } else {
                                               whenNotGrouped; 
                                             }

  
  
  /**
   * check - are we have instance of clazz somewhere in our package structure
   * upper to current class.
   *@param clazz - class to search
   *@param classLoader - class loader to search
   * [todo - check same fixture state types (?)]
   */
  def findSpecGroupWith(clazz:Class[_], classLoader: ClassLoader):Option[Class[_]] =
  {
    val pkg = this.getClass.getPackage;
    val components = pkg.getName().split('.');
    val retval: Option[Class[_]] = if (components.length > 1) {
      var i=components.length;
      var isFound = false;
      var foundClass: Option[Class[_]] = None;
      while( i > 0 && !isFound ) {
         isFound = ! ( ReflectionUtils.findClasses(components.take(i).mkString("."),
                                                 { (x: Class[_]) =>
                                                    if (clazz.isAssignableFrom(x)) {
                                                       foundClass = Some(x);
                                                       false;
                                                    }else{
                                                       true;
                                                    }
                                                 }, false) );
         i=i-1;
      }
      foundClass
    }else{
      None
    }
    retval;
  }
  
  def runGrouped[T](testName: Option[String], reporter: Reporter, stopper: Stopper, 
                             filter: Filter, configMap: Map[String, Any], 
                             distributor: Option[Distributor], tracker: Tracker, 
                             internalSpec: fixture.Suite, fixtureGroupClass: Class[T]): Unit = {
     if (isGrouped) {
        if (findSpecGroupWith(fixtureGroupClass, this.getClass.getClassLoader)!=None) {
           /* do nothing:  we situate in scope of group which will run us */
        } else {
          throw new IllegalStateException(
               """
                 For use managed fixture you must or define group class ("%s" in this or
                 enclosing package, or mark execution autonomic if you want autonomic execution
               """.format(fixtureGroupClass.getSimpleName())
              );
        }
     } else {
        internalSpec.run(testName, reporter, stopper, filter, configMap, distributor, tracker) 
     }
  }
    
  
  
}
