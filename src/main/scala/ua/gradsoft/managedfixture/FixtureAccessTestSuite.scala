package ua.gradsoft.managedfixture

abstract class FixtureAccessTestSuite[A,Fixture,State]
{

   def operations: Seq[FixtureAccessOperation[A,Fixture,State]]

}
