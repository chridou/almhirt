package almhirt.http

import almhirt.common._

trait ResponseConsumer[T] {
  final def apply(responder: T, response: HttpResponse){ letConsume(responder, response) }
  def letConsume(responder: T, response: HttpResponse)
}