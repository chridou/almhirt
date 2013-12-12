package almhirt.http

object AlmMediaTypes extends AlmMediaTypesRegistry {
  def registeredJsonStructuredMedia(vendor: MediaTypeVendorPart, content: String): AlmMediaType =
    AlmMediaType.applicationStructured(vendor, content, "json").registered
    
  def registeredXmlStructuredMedia(vendor: MediaTypeVendorPart, content: String): AlmMediaType =
    AlmMediaType.applicationStructured(vendor, content, "xml").registered

  def registeredMessagePackStructuredMedia(vendor: MediaTypeVendorPart, content: String): AlmMediaType =
    AlmMediaType.applicationStructured(vendor, content, "msgpack").registered
    
  val `text/html` = 
    AlmMediaType.text("html")
    	.makeIanaRegistered
    	.addFileExtensions("htm", "html")
    	.registered

  val `text/xml` = 
    AlmMediaType.text("xml")
    	.makeIanaRegistered
    	.addFileExtensions("xml")
    	.registered

  val `text/json` = 
    AlmMediaType.text("json")
    	.makeCompressible
    	.makeIanaRegistered
    	.addFileExtensions("json")
    	.registered

  val `application/xml` = 
    AlmMediaType.applicationTextual("xml")
    	.makeIanaRegistered
    	.addFileExtensions("xml")
    	.registered

  val `application/json` = 
    AlmMediaType.applicationTextual("json")
    	.makeIanaRegistered
    	.addFileExtensions("json")
    	.registered
    	
  val `application/x-msgpack` = 
    AlmMediaType.applicationBinary("x-msgpack")
    	.makeCompressible
    	.addFileExtensions("msgpack")
    	.registered
  
}