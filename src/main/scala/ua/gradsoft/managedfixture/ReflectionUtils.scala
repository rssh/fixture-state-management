package ua.gradsoft.managedfixture

import java.io._
import java.util.zip._

/**
 * helper class, which simplicify work with reflection
 */
object ReflectionUtils {
  
  /**
   * find all classes witch satisficy some condition. 
   * @param classLoader - classLoader to searchh
   * @param packageName - name to search
   * @param testFun - name to test. testFun must return true to continute search or false to
   *                  stop.
   * @param recursive - search recursive in subdirs
   * @return last value of more flag.
   */
  def findClasses(packageName:String, testFun: Class[_]=>Boolean, recursive:Boolean): Boolean =
  {
    val e = classLoader.getResources(packageName.replace(".","/"));
    var more = true;
    var nIterations = 0;
    while(e.hasMoreElements && more) {
      val url = e.nextElement();
      more = forClassesInDir(packageName,url.getFile, testFun, recursive);
      nIterations += 1;
    }
    more
  }
  
  private[this] def forClassesInDir(pkgName:String, 
                                    dir:String, 
                                    fun: Class[_]=>Boolean, 
                                    recursive: Boolean):Boolean =
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
