package marketpredictions.engine

import marketpredictions.db._
import marketpredictions.db.MPSchema._

import org.squeryl._;
import org.squeryl.dsl._;
import org.squeryl.PrimitiveTypeMode._;


trait UserOperations
{

 this: Api => 


 def createUser(name: String): Long =
  inTransaction {
    val newUser = members insert Member(-1L, name, BigDecimal(0L));
    newUser.id
  }

 def dropUser(id: Long): Boolean =
  inTransaction {
    members.deleteWhere(u => (u.id === id)) != 0
  }

}

// vim: set ts=4 sw=4 et:
