package ua.gradsoft.managedfixture

import java.sql.Connection;

/**
 * FixtureAccess helper for relation databases.
 *It can be mixed in you implementation of fixture acceess for
 *functionality of restoring and saving information about states.
 *
 */
trait RdbAccessHelper[T <: FixtureStateTypes]
{

  this: FixtureAccess[T] => 

  /**
   * name of table, where test states information is live.
   * by default: test_states.
   */
  def testStatesTableName = "test_states";
  
  def acquireJdbcConnection: Connection;

  def releaseJdbcConnection(cn: Connection)
  { cn.close(); }

  /**
   * output DDL for creating of table for test states,
   * which have next strcture:
   * {{{
   *  create table {testStatesTableName} (
   *    rtype   VARCHAR(6) not null,
   *    value   VARCHAR(128) not null,
   *     primary key(rtype, value)
   *  );
   * }}}
   */
  def testStatesCreateTableDdl: String =
  {
     """
        create table %s(
          rtype  varchar(6) not null,
          value  varchar(128) not null,
          primary key(rtype,value)
        );
     """.format(testStatesTableName);
  }

  /**
   * create test states table if it not exists yet.
   */
  def createTestStatesTableIfNeeded:Unit =
  {
   val cn = acquireJdbcConnection;
   try{
     createTestStatesTableIfNeeded(cn:Connection);
   }finally{
     releaseJdbcConnection(cn);
   }
  }

  private def createTestStatesTableIfNeeded(cn:Connection) :Unit =
  {
   val rs = cn.getMetaData.getTables(null,null,testStatesTableName,Array[String]("TABLE"));
   if (!rs.next) {
     val st = cn.createStatement();
     st.executeUpdate(testStatesCreateTableDdl);
   }
  }

  /**
   * output DDL for dropping of test states table
   */
  def testStatesDropTableDdl: String =
  {
    "drop table "+testStatesTableName;
  }


  def dropTestStatesTable:Unit =
  {
   val cn = acquireJdbcConnection;
   try{
     val st = cn.createStatement();
     st.executeUpdate(testStatesDropTableDdl);
   }finally{
     releaseJdbcConnection(cn);
   }
  }


  override def current:Option[(T#StartStateType,Set[T#StateAspectType])] =
  {
   if (cashedCurrent==None) {
    var s: Option[T#StartStateType] = None;
    var a: Set[T#StateAspectType] = Set();
    val cn = acquireJdbcConnection;
    try {
     createTestStatesTableIfNeeded(cn);
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
        throw new IllegalStateException("invaid type of items in "+testStatesTableName);
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
    //TODO: think how to log.
    //System.err.println("markStateChanges:"+stateChange+", "+stateAspectChanges);
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
                                    """insert into %s(rtype, value)
                                                values('ASPECT',?)
                                     """.format(testStatesTableName));
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
                 st.executeUpdate("delete from "+testStatesTableName);
                 val st1 = cn.prepareStatement("""
                                   insert into %s(rtype,value)
                                           values('STATE',?);
                                """.format(testStatesTableName));
                 st1.setString(1,x.toString);
                 st1.executeUpdate();
              } finally {
                 releaseJdbcConnection(cn);
              }
              cashedCurrent = Some((x,stateAspectChanges));
      case UndefinedState => 
              val cn = acquireJdbcConnection;
              try {
                cn.createStatement.executeUpdate("delete from "+testStatesTableName);
              } finally {
                releaseJdbcConnection(cn);
              }
              cashedCurrent = None;
    }
  }


  var cashedCurrent:Option[(StartStateType,Set[T#StateAspectType])] = None;

}

// vim: set ts=4 sw=4 et:
