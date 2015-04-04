package ua.gradsoft.managedfixture

trait IndexedByFixtureUsage[A,S]
{
  def usage: FixtureStateUsageDescription[S]

  def value:A
}
