package almhirt.http

import almhirt.common._

sealed trait HttpClientTrustLevel
/** Return the complete problem as a response
 */
case object FullTrust extends HttpClientTrustLevel
/** Return the problem with its message only
 */
case object Careful extends HttpClientTrustLevel
/** All problems become unspecified Problems. All application problems map to 404-Not_Found, all system problems map to 500-Internal_Server_Error
 */
case object Paranoid extends HttpClientTrustLevel

trait ProblemLaundry extends PartialFunction[Problem, (Problem, HttpError)] {
  def trustLevel: HttpClientTrustLevel
}

trait ProblemLaundryFactory {
  /**
   * May return a more secure laundry, but never a less secure one.
   */
  def createProblemLaundry(trustLevel: HttpClientTrustLevel): ProblemLaundry
}