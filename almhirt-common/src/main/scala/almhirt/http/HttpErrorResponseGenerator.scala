package almhirt.http

import almhirt.common.Problem

trait HttpErrorResponseGenerator {
  final def apply(problem: Problem, channel: String): HttpResponse = generateErrorResponse(problem, channel)
  def generateErrorResponse(problem: Problem, channel: String): HttpResponse
}