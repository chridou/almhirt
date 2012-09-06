package almhirt.almvalidation

import almhirt._

trait ProblemDefaults {
  val defaultSystemProblem = UnspecifiedProblem("unspecified system problem", category = SystemProblem)
  val defaultApplicationProblem = UnspecifiedProblem("unspecified application problem", category = ApplicationProblem)
  implicit val defaultProblem = defaultSystemProblem
}

object ProblemDefaults extends ProblemDefaults