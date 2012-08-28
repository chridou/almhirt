package almhirt.validation

trait ProblemFunctions {
  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  
  val defaultSystemProblem = UnspecifiedProblem("unspecified system problem", category = SystemProblem)
  val defaultApplicationProblem = UnspecifiedProblem("unspecified application problem", category = ApplicationProblem)
  val defaultProblem = defaultSystemProblem
}

object ProblemFunctions extends ProblemFunctions