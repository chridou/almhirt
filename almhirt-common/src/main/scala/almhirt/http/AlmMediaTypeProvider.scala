package almhirt.http

import scalaz._, Scalaz._
import almhirt.common._

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
  
  def findMediaTypeForMarshalling[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findForMarshalling(contentFormat)
  }

  def getMediaTypeForMarshalling[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getForMarshalling(contentFormat)
  }

  def findMediaTypeForMarshalling[T: AlmMediaTypesProvider](mainType: String, contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findForMarshalling(mainType, contentFormat)
  }

  def getMediaTypeForMarshalling[T: AlmMediaTypesProvider](mainType: String, contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getForMarshalling(mainType, contentFormat)
  }

  def findAppMediaTypeForMarshalling[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findAppForMarshalling(contentFormat)
  }

  def getAppMediaTypeForMarshalling[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getAppForMarshalling(contentFormat)
  }
  
  implicit class AlmMediaTypesProviderOps[T](self: AlmMediaTypesProvider[T]) {
    def withGenericMarshalling = new AlmMediaTypesProvider[T] {
      val marshallableMediaTypes = self.marshallableMediaTypes ++ genericMarschallingAppendix
      val unmarshallableMediaTypes = self.unmarshallableMediaTypes
    }

    def getForMarshalling(contentFormat: String): AlmValidation[AlmMediaType] = {
      self.marshallableMediaTypes.find(_.contentFormat == contentFormat) match {
        case Some(mt) => mt.success
        case None => NoSuchElementProblem(s"""No media type has a content format "$contentFormat".""").failure
      }
    }

    
    def findForMarshalling(contentFormat: String): Option[AlmMediaType] =
      getForMarshalling(contentFormat).fold(
        _ => None,
        succ => Some(succ))

    def getForMarshalling(mainType: String, contentFormat: String): AlmValidation[AlmMediaType] = {
      self.marshallableMediaTypes.find(mt => mt.mainType == mainType && mt.contentFormat == contentFormat) match {
        case Some(mt) => mt.success
        case None => NoSuchElementProblem(s"""No media type in media range "$mainType" has a content format "$contentFormat".""").failure
      }
    }

    def findForMarshalling(mainType: String, contentFormat: String): Option[AlmMediaType] =
      getForMarshalling(mainType, contentFormat).fold(
        _ => None,
        succ => Some(succ))

    def getAppForMarshalling(contentFormat: String) = getForMarshalling("application", contentFormat)
    def findAppForMarshalling(contentFormat: String) = findForMarshalling("application", contentFormat)
  
    def defaultForMarshalling: Option[AlmMediaType] = self.marshallableMediaTypes.headOption
    def defaultForUnmarshalling: Option[AlmMediaType] = self.unmarshallableMediaTypes.headOption
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