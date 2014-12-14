package marketpredictions.engine

import org.squeryl._;
import org.squeryl.adapters._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;

import java.sql._;


trait SimpleEngineContext extends EngineContext
{

   def onInit: Unit =
   {
    Class.forName("org.h2.Driver" );
    SessionFactory.concreteFactory = Some(()=>
                      Session.create(sqlConnection,
                      new H2Adapter));
   }

   def onShutdown: Unit = {}

   def now = new Timestamp(System.currentTimeMillis);

   def now_=(x: Timestamp):Unit = throw new UnsupportedOperationException();

   def sqlConnection = DriverManager.getConnection("jdbc:h2:test", "sa", "");

}

// vim: set ts=4 sw=4 et:
