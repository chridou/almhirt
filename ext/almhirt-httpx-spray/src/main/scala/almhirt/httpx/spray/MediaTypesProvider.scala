package almhirt.httpx.spray

import spray.http.MediaType
import spray.http.MediaTypes
import almhirt.http._

trait MediaTypesProvider[T] {
  def marshallableMediaTypes: Seq[MediaType]
  def unmarshallableMediaTypes: Seq[MediaType]
}

object MediaTypesProvider {
  def apply[T](theMarshallableMediaTypes: Seq[MediaType], theUnmarshallableMediaTypes: Seq[MediaType]): MediaTypesProvider[T] =
    new MediaTypesProvider[T] {
      val marshallableMediaTypes = theMarshallableMediaTypes
      val unmarshallableMediaTypes = theUnmarshallableMediaTypes
    }
  
  def defaults[T](forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): MediaTypesProvider[T] = {
    val registeredDefaults = AlmhirtMediaTypes.createDefaultMediaTypes(forType)
    val theMarshallableMediaTypes = registeredDefaults ++ Seq(MediaTypes.`application/json`, MediaTypes.`application/xml`, AlmhirtMediaTypes.`application/x-msgpack`, MediaTypes.`text/html`)
    val theUnmarshallableMediaTypes = registeredDefaults
    new MediaTypesProvider[T] {
      val marshallableMediaTypes = theMarshallableMediaTypes
      val unmarshallableMediaTypes = theUnmarshallableMediaTypes
    }
  }

  def qualifiedOnlyDefaults[T](forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): MediaTypesProvider[T] = {
    val registeredDefaults = AlmhirtMediaTypes.createDefaultMediaTypes(forType)
    val theMarshallableMediaTypes = registeredDefaults
    val theUnmarshallableMediaTypes = registeredDefaults
    new MediaTypesProvider[T] {
      val marshallableMediaTypes = theMarshallableMediaTypes
      val unmarshallableMediaTypes = theUnmarshallableMediaTypes
    }
  }
  
}