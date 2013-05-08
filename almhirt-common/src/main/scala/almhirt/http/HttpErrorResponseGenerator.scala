package almhirt.http

import almhirt.common._

trait HttpErrorResponseGenerator {
  final def apply(problem: Problem, channel: String)(implicit problemConsumer: Consumer[Problem]): HttpResponse = generateErrorResponse(problem, channel)
  def generateErrorResponse(problem: Problem, channel: String)(implicit problemConsumer: Consumer[Problem]): HttpResponse
}