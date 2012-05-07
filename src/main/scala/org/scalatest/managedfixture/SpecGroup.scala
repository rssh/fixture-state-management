package org.scalatest.managedfixture

import ua.gradsoft.managedfixture._
import scala.util.DynamicVariable

trait SpecGroup 
{


  def checkClass[T](cl:Class[T]): Boolean 
     = true;
  
  def checkObject(x:AnyRef): Boolean
    = true;

  private[scalatest] def collectGrouped[T](self: AnyRef, cl:Class[T]):Boolean =
  {
       ReflectionUtils.findClasses(
                          self.getClass().getClassLoader(),
                          self.getClass().getPackage().getName,
                       {
                         (x:Class[_]) => 
                           if ( cl.isAssignableFrom(x) && classOf[Grouped].isAssignableFrom(x)
                                && ! classOf[SpecGroup].isAssignableFrom(x) ) {
                              if (checkClass(x)) {
                                GroupSpecConstructorKluge.currentOwner.withValue(Some(this)) {
                                 val obj = x.newInstance().asInstanceOf[Grouped];
                                 obj.mark(this);
                                }
                              }
                           }
                           true
                        },
                        true);
  }


}

private[scalatest] object GroupSpecConstructorKluge
{
  val currentOwner = new DynamicVariable[Option[SpecGroup]](None);
}
