package almhirt.http.impl

import almhirt.common.Problem
import almhirt.http._

object JustForTestingProblemLaundry extends ProblemLaundry {
  val trustLevel = FullTrust
  def apply(problem: Problem) = (problem, Http_500_Internal_Server_Error)
  def isDefinedAt(problem: Problem) = true
}