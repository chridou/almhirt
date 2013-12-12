package almhirt.http

trait AlmMediaTypesProvider[T] {
  def marshallableMediaTypes: Seq[AlmMediaType]
  def unmarshallableMediaTypes: Seq[AlmMediaType]
}

object AlmMediaTypesProvider {
  def apply[T](theMarshallableMediaTypes: Seq[AlmMediaType], theUnmarshallableMediaTypes: Seq[AlmMediaType]): AlmMediaTypesProvider[T] =
    new AlmMediaTypesProvider[T] {
      val marshallableMediaTypes = theMarshallableMediaTypes
      val unmarshallableMediaTypes = theUnmarshallableMediaTypes
    }

    def registeredDefaults[T](content: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): AlmMediaTypesProvider[T] = {
      val mediaTypes = createRegisteredDefaults(mtvp.vendor, content)
      new AlmMediaTypesProvider[T] {
        val marshallableMediaTypes = mediaTypes
        val unmarshallableMediaTypes = mediaTypes
      }
    }

  implicit class AlmMediaTypesProviderOps[T](self: AlmMediaTypesProvider[T]) {
    def withGenericMarshalling = new AlmMediaTypesProvider[T] {
        val marshallableMediaTypes = self.marshallableMediaTypes ++ genericMarschallingAppendix
        val unmarshallableMediaTypes = self.unmarshallableMediaTypes
      }
      
  }
    
  def createRegisteredDefaults(vendor: MediaTypeVendorPart, content: String): Seq[AlmMediaType] =
    Seq(AlmMediaTypes.registeredMessagePackStructuredMedia(vendor, content),
      AlmMediaTypes.registeredJsonStructuredMedia(vendor, content),
      AlmMediaTypes.registeredXmlStructuredMedia(vendor, content))
      
  val genericMarschallingAppendix = Seq(
      AlmMediaTypes.`text/html`, 
      AlmMediaTypes.`application/x-msgpack`, 
      AlmMediaTypes.`application/json`, 
      AlmMediaTypes.`application/xml`, 
      AlmMediaTypes.`text/json`, 
      AlmMediaTypes.`text/xml`)
}