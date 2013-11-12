package almhirt.common

class EscalatedProblemException(val escalatedProblem: Problem) extends Exception(s"""A validation failed and the problem has been escalated: "${escalatedProblem.message}"""") {
  override def toString(): String = {
    super.toString + "\nEscalated problem:\n" + escalatedProblem.toString()
  }
}