package almhirt.http

import almhirt.common.Problem

trait HttpErrorResponseGenerator {
  final def apply(problem: Problem): HttpResponse = generateErrorResponse(problem)
  def generateErrorResponse(problem: Problem): HttpResponse
}