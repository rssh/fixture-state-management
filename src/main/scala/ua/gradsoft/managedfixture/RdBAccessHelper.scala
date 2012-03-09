package ua.gradsoft.managedfixture

import java.sql.Connection;

/**
 * Helper for implementing FixtureAccess for relation database.
 *Usually when we use RDB as managedfixture, we add own table
 * which used for storing db states. 
 */
trait RdbAccessHelper[T <: FixtureStateTypes]
{

  this: FixtureAccess[T] => 

  val fixtureStateTypes: T ;

  def testStatesTableName = "test_states";
  
  def acquireJdbcConnection: Connection;

  def releaseJdbcConnection(cn: Connection)
  { cn.close(); }

  override def current:Option[(T#StartStateType,Set[T#StateAspectType])] =
  {
   if (cashedCurrent==None) {
    var s: Option[T#StartStateType] = None;
    var a: Set[T#StateAspectType] = Set();
    val cn = acquireJdbcConnection;
    try {
     var st = cn.createStatement();
     var rs = st.executeQuery("select * from %s".format(testStatesTableName));
     while(rs.next) {
      val sv = rs.getString("value");
      if (rs.getString("rtype")=="STATE") {
         val si = fixtureStateTypes.startStates.values.find( _.toString == sv )
         if (si==None) {
           throw new IllegalStateException("Invalid state "+rs.getString("value")+" for test database");
         } 
         if (s==None) {
             s = si
         } else {
           throw new IllegalStateException("Two states in the set database");
         }
      } else if (rs.getString("rtype")=="ASPECT") {
         a = a ++ fixtureStateTypes.stateAspects.values.find(_.toString == sv);
      } else {
        throw new IllegalStateException("invaid type of items in test_states");
      }
     }
    } finally {
     releaseJdbcConnection(cn);
    }
    //bug in scala compiler:
    //cashedCurrent = s.map( (_,a) );
    //
    if (s.isEmpty) {
      cashedCurrent = None
    } else {
      cashedCurrent = Some((s.get,a))
    }
   }
   cashedCurrent;
  }


  override def markStateChanges(stateChange:FixtureStateChange[T], 
                                stateAspectChanges:Set[T#StateAspectType]) =
  {
    val prevState = current;
    stateChange match {
      case SameState => 
              val prevAspects = prevState.map(_._2).getOrElse(Set[T#StateAspectType]());
              var toAdd:Set[T#StateAspectType] = Set();
              for(a <- stateAspectChanges) {
                  if (!prevAspects.contains(a)) {
                         toAdd = toAdd + a;
                  }
              }
              if (!toAdd.isEmpty) {
                 val cn = acquireJdbcConnection
                 try {
                   for(a <- toAdd) {
                     val st = cn.prepareStatement(
                                    """insert into test_states(rtype, value)
                                                values('ASPECT',?)
                                     """);
                     st.setString(1,a.toString);
                     st.executeUpdate();
                   }
                 } finally {
                   releaseJdbcConnection(cn);
                 }
              }
              // compiler bug.
              // cashedCurrent = cashedCurrent.map( x => (x._1, x._2 union toAdd))
              if (!cashedCurrent.isEmpty) {
                    cashedCurrent = Some((cashedCurrent.get._1, cashedCurrent.get._2 union toAdd))
              }
      case NewState(x) => 
              var cn = acquireJdbcConnection;
              try {
                 val st = cn.createStatement;
                 st.executeUpdate("delete from test_states");
                 val st1 = cn.prepareStatement("""
                                   insert into test_states(rtype,value)
                                           values('STATE',?);
                                """);
                 st1.setString(1,x.toString);
                 st1.executeUpdate();
              } finally {
                 releaseJdbcConnection(cn);
              }
              cashedCurrent = Some((x,stateAspectChanges));
      case UndefinedState => 
              val cn = acquireJdbcConnection;
              try {
                cn.createStatement.executeUpdate("delete from test_states");
              } finally {
                releaseJdbcConnection(cn);
              }
              cashedCurrent = None;
    }
  }



  var cashedCurrent:Option[(StartStateType,Set[T#StateAspectType])] = None;

}

// vim: set ts=4 sw=4 et:
