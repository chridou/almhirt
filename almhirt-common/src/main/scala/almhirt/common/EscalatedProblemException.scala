package almhirt.common

final case class EscalatedProblemException(val escalatedProblem: Problem) extends Exception(
    s"""	|A validation failed and the problem has been escalated:
					|${escalatedProblem}""".stripMargin) {
}