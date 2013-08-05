package almhirt.corex.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

object CommandMarshalling extends MarshallingFactory[Command]