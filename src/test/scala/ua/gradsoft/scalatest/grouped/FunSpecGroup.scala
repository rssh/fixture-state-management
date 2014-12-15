package ua.gradsoft.scalatest.grouped

import org.scalatest._
import ua.gradsoft.scalatest.state.internal._

class FunSpecGroup extends managedfixture.FunSpecGroup[Base1FixtureStateInfo.type]
{

  val fixtureStateTypes = Base1FixtureStateInfo;
  val fixtureAccess = Base1FixtureAccess;


  // this is needed only to test our runner. I.e. in non-internal tests overriding run
  //   is not needed. 
  override def run(testName: Option[String], args: Args): Status =
  {
        //System.err.println("funspec begin");
        val retval = super.run(testName, args);
        import FunSpecGroup._
        assert(t1s1done,"t1s1 test was not passed");
        assert(t1s2done,"t1s2 test was not passed");
        assert(t2s1done,"t2s1 test was not passed");
        assert(t2s2done,"t2s2 test was not passed");
        //System.err.println("funspec end");
        retval
  }


}

object FunSpecGroup
{

  // setted to true in tests. can 
  //  static, becouse only one test at all can touch this variable.

  @volatile var t1s1done = false;
  @volatile var t1s2done = false;
  @volatile var t2s1done = false;
  @volatile var t2s2done = false;


}

// vim: set ts=4 sw=4 et:
