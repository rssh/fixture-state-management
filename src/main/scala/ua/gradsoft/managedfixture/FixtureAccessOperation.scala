package ua.gradsoft.managedfixture


case class FixtureAccessOperation[T <: FixtureStateTypes, A](f: T#Fixture => A, change: FixtureStateChange[T])
