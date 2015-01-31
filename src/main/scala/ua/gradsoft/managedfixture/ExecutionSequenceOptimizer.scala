package ua.gradsoft.managedfixture

case class StatedOperations[A,Fixture,State]( op: Seq[FixtureAccessOperation[A,Fixture,State]],
                                              state: State)
                            

class ExecutionSequenceOptimizer[A,Fixture,State](transitions: StateTransitions[A,Fixture,State])
{

 final val SEARCH_WIDTH = 10

 type Operation = FixtureAccessOperation[A,Fixture,State]


}
