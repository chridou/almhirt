package riftwarpx.http.spray

import spray.httpx.marshalling.Marshaller
import almhirt.http.AlmMediaTypes
import almhirt.httpx.spray.marshalling.{ MarshallerFactory, UnmarshallerFactory }
import almhirt.http._
import riftwarp._
import spray.http.ContentType
import spray.http.HttpEntity
import spray.httpx.unmarshalling._
import spray.http.HttpEntity._
import spray.http.HttpCharsets

object WarpPackageMarshalling {
  import almhirt.httpx.spray.marshalling.Helper._

  private val sprayMarshallableContentTypeTypes: Seq[ContentType] = almhirt.httpx.spray.almMediaTypes2SprayContentTypes(Seq(
    AlmMediaTypes.`text/html`,
    AlmMediaTypes.`text/xml`,
    AlmMediaTypes.`application/xml`,
    AlmMediaTypes.`text/json`,
    AlmMediaTypes.`application/json`,
    AlmMediaTypes.`application/x-msgpack`))

  private val sprayUnmarshallableContentTypeTypes: Seq[ContentType] = almhirt.httpx.spray.almMediaTypes2SprayContentTypes(Seq(
    AlmMediaTypes.`text/xml`,
    AlmMediaTypes.`application/xml`,
    AlmMediaTypes.`text/json`,
    AlmMediaTypes.`application/json`,
    AlmMediaTypes.`application/x-msgpack`))

  def marshaller(riftWarp: RiftWarp): Marshaller[WarpPackage] =
    marshaller(riftWarp.dematerializers)

  def marshaller(dematerializers: Dematerializers): Marshaller[WarpPackage] =
    marshaller(new WarpPackageHttpSerializer(dematerializers))

  def marshaller(wireSerializer: HttpSerializer[WarpPackage]): Marshaller[WarpPackage] =
    new MarshallerFactory[WarpPackage] {}.marshaller(wireSerializer, sprayMarshallableContentTypeTypes: _*)

  def unmarshaller(riftWarp: RiftWarp): Unmarshaller[WarpPackage] =
    unmarshaller(riftWarp.rematerializers)

  def unmarshaller(rematerializers: Rematerializers): Unmarshaller[WarpPackage] =
    unmarshaller(new WarpPackageHttpDeserializer(rematerializers))

  def unmarshaller(wireSerializer: HttpDeserializer[WarpPackage]): Unmarshaller[WarpPackage] =
    new UnmarshallerFactory[WarpPackage] {}.unmarshaller(wireSerializer, sprayUnmarshallableContentTypeTypes: _*)
}