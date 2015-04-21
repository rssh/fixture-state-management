package org.scalatest.managedfixture


object  ReflectUtil
{

   def constructor3[X,A1,A2,A3](xClass: Class[X], a1:A1, a2:A2, a3:A3): X =
   {
    import scala.reflect.runtime.{universe => ru}
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val classSymbol = mirror.classSymbol(xClass)
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
    val instance = constructorMirror.apply(a1,a2,a3)
    instance.asInstanceOf[X]
   }


}
