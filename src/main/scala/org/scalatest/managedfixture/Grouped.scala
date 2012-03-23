package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._

trait Grouped {

  
  
  /**
   * check - are we have instance of clazz somewhere in our package structure
   * upper to current class.
   * [todo - check same fixture state types (?)]
   */
  def checkGroupExists(clazz:Class[_]):Boolean =
  {
    val pkg = clazz.getPackage();
    val components = pkg.getName().split('.');
    if (components.length > 1) {
      var i=components.length;
      var isFound = false;
      while( i > 0 && !isFound ) {
         isFound = ! ( ReflectionUtils.findClasses(components.take(i).mkString("."),
                                                 { (x: Class[_]) =>
                                                   !(classOf[FlatSpecGroup[_]].isAssignableFrom(x))
                                                 }, false) );
      }
      isFound;
    }else{
      false
    }
  }
  
  def mark(owner:SpecGroup):Unit = {}
  
}