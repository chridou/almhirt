package almhirt.corex.spray.marshalling

import almhirt.common._
import spray.httpx.marshalling.Marshaller
import spray.http.ContentTypes
import spray.http.HttpEntity


object CommandMarshalling {
  def stringMarshaller(serializer: almhirt.serialization.CommandStringSerializer, contentTypes: ContentType*): Marshaller[Command] =
    ???
    
implicit val StringMarshaller =
  Marshaller.of[String](ContentTypes.`text/plain`) { (value, contentType, ctx) ⇒
    ctx.marshalTo(HttpEntity(contentType, value))
  }  
}