package ua.gradsoft.managedfixture

import java.io._
import java.util.zip._

/**
 * helper class, which simplicify work with reflection
 */
object ReflectionUtils {
  
  
  def findClasses(packageName:String, testFun: Class[_]=>Boolean, recursive:Boolean):Boolean =
  {
    val e = classLoader.getResources(packageName);
    var more = true;
    while(e.hasMoreElements && more) {
      val url = e.nextElement();
      more = forClassesInDir(packageName,url.getFile, testFun, recursive);
    }
    more
  }
  
  private[this] def forClassesInDir(pkgName:String, dir:String, fun: Class[_]=>Boolean, recursive: Boolean):Boolean =
  {
    var more = true;
    if (dir.startsWith("file:") && dir.contains("!") ) {
      val jar = new java.net.URL(dir.split("!")(0));
      val zip = new ZipInputStream(jar.openStream());
      var zipEntry = zip.getNextEntry;
      while(zipEntry!=null && more) {
        val className = zipEntry.getName.replaceAll("[$].*", "").replace('/', '.');
        if (className.endsWith(".class")) {
          
            val c = try {
                       Some(Class.forName(className))
                    } catch {
                       case ex: ClassNotFoundException => None                      
                    }
            if (c.isDefined) {        
              more = fun(c.get)
            }
        }
        zipEntry = zip.getNextEntry;
      }
    } else {
      var fd = new File(dir);
      if (fd.exists) {
        for(f <- fd.listFiles) {
          var fname = f.getName;
          if (fname.endsWith(".class")) {
            val className = pkgName+"."+ fname.substring(0, fname.length-6);
            val c = try {
                      Some(Class.forName(className))
                    }catch{
                      case ex: ClassNotFoundException => None
                    }
            if (c.isDefined) {        
              more = fun(c.get);
            }
          } else if (f.isDirectory() && recursive) {
            more = forClassesInDir(pkgName+"."+f.getName(),dir+"/"+f.getName,fun,recursive)
          }
          if (!more) {
            return false;
          }
        }
      }
    }
    more;
  }
  
  private[this] def classLoader: ClassLoader =
  {
   Option(Thread.currentThread().getContextClassLoader()) match {
      case Some(x) => x
      case None => this.getClass.getClassLoader();
   }
  }

  

}