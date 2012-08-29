package almhirt.validation

trait ProblemFunctions {
  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
}

object ProblemFunctions extends ProblemFunctions