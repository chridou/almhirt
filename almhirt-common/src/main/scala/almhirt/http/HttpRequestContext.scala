package almhirt.http

import almhirt.common._

trait ContextInstances[T] {
  def contentTypeExtractor: HttpContentTypeExtractor[T]
  def payloadExtractor: PayloadExtractor[T]
}

trait HttpInstances {
  def channelExtractor: ChannelExtractor
  def classifiesChannels: ClassifiesChannels
  def errorResponseGenerator: HttpErrorResponseGenerator
}

object HttpContext {
  def begin[T, TIn](from: T)(f: T => AlmFuture[TIn]): AlmFuture[TIn] =
    f(from)

  def request[T, TIn](f: HttpRequest => AlmFuture[TIn])(implicit context: ContextInstances[T], instances: HttpInstances): T => AlmFuture[TIn] = {
    (from: T) =>
      {
        (for {
          contentType <- context.contentTypeExtractor(from)
          channel <- instances.channelExtractor(contentType)
          contentTypeClassifier <- instances.classifiesChannels(channel)
          payload <- context.payloadExtractor(from, contentTypeClassifier)
        } yield HttpRequest(HttpContent(contentType, payload), Nil)).fold(
          fail => AlmFuture.failed(fail),
          succ => f(succ))
      }
  }

  def unmarshal[TIn](implicit unmarshaller: HttpUnmarshaller[TIn], hec: HasExecutionContext): HttpRequest => AlmFuture[TIn] =
    (req: HttpRequest) => AlmFuture{ unmarshaller(req) }
    
  def process[U,V](f: U => AlmFuture[V]): 
}