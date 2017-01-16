package riftwarpx.http.akkahttp

import almhirt.http.AlmMediaTypes
import almhirt.httpx.akkahttp.marshalling.{ MarshallerFactory, UnmarshallerFactory }
import almhirt.http._
import riftwarp._
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import scala.concurrent.ExecutionContext
import akka.stream.Materializer

object WarpPackageMarshalling {
  import almhirt.httpx.akkahttp.marshalling.Helper._

  private val akkaHttpMarshallableContentTypeTypes: Seq[ContentType] = almhirt.httpx.akkahttp.almMediaTypes2AkkaHttpContentTypes(Seq(
    AlmMediaTypes.`text/html`,
    AlmMediaTypes.`text/xml`,
    AlmMediaTypes.`application/xml`,
    AlmMediaTypes.`text/json`,
    AlmMediaTypes.`application/json`,
    AlmMediaTypes.`application/x-msgpack`))

  private val akkaHttpUnmarshallableContentTypeTypes: Seq[ContentType] = almhirt.httpx.akkahttp.almMediaTypes2AkkaHttpContentTypes(Seq(
    AlmMediaTypes.`text/xml`,
    AlmMediaTypes.`application/xml`,
    AlmMediaTypes.`text/json`,
    AlmMediaTypes.`application/json`,
    AlmMediaTypes.`application/x-msgpack`))

  def marshaller(riftWarp: RiftWarp): ToEntityMarshaller[WarpPackage] =
    marshaller(riftWarp.dematerializers)

  def marshaller(dematerializers: Dematerializers): ToEntityMarshaller[WarpPackage] =
    marshaller(new WarpPackageHttpSerializer(dematerializers))

  def marshaller(wireSerializer: HttpSerializer[WarpPackage]): ToEntityMarshaller[WarpPackage] = {
    val marshallers = akkaHttpMarshallableContentTypeTypes.map(contentType => new MarshallerFactory[WarpPackage] {}.marshaller(wireSerializer, contentType)).toList
    Marshaller.oneOf(marshallers: _*)
  }

  def unmarshaller(riftWarp: RiftWarp)(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[WarpPackage] =
    unmarshaller(riftWarp.rematerializers)

  def unmarshaller(rematerializers: Rematerializers)(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[WarpPackage] =
    unmarshaller(new WarpPackageHttpDeserializer(rematerializers))

  def unmarshaller(wireSerializer: HttpDeserializer[WarpPackage])(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[WarpPackage] =
    new UnmarshallerFactory[WarpPackage] {}.unmarshaller(wireSerializer, akkaHttpUnmarshallableContentTypeTypes)
}