package almhirt.http

import scalaz._, Scalaz._
import almhirt.common._

trait AlmMediaTypesProvider[T] {
  def targetMediaType: AlmMediaType
  def sourceMediaTypes: Seq[AlmMediaType]
}

object AlmMediaTypesProvider {
  def apply[T](theMarshallableMediaType: AlmMediaType, theUnmarshallableMediaTypes: Seq[AlmMediaType]): AlmMediaTypesProvider[T] =
    new AlmMediaTypesProvider[T] {
      val targetMediaType = theMarshallableMediaType
      val sourceMediaTypes = theUnmarshallableMediaTypes
    }

  def registeredDefaults[T](content: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): AlmMediaTypesProvider[T] = {
    val mediaTypes = createRegisteredDefaults(mtvp.vendor, content)
    new AlmMediaTypesProvider[T] {
      val targetMediaType = AlmMediaTypes.`application/json`
      val sourceMediaTypes = mediaTypes
    }
  }

  def findMediaTypeForSerialization[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findForSerialization(contentFormat)
  }

  def getMediaTypeForSerialization[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getForSerialization(contentFormat)
  }

  def findMediaTypeForSerialization[T: AlmMediaTypesProvider](mainType: String, contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findForSerialization(mainType, contentFormat)
  }

  def getMediaTypeForSerialization[T: AlmMediaTypesProvider](mainType: String, contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getForSerialization(mainType, contentFormat)
  }

  def findAppMediaTypeForSerialization[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.findAppForSerialization(contentFormat)
  }

  def getAppMediaTypeForSerialization[T: AlmMediaTypesProvider](contentFormat: String) = {
    val mtp = implicitly[AlmMediaTypesProvider[T]]
    mtp.getAppForSerialization(contentFormat)
  }

  implicit class AlmMediaTypesProviderOps[T](self: AlmMediaTypesProvider[T]) {
    def withGenericTargets = new AlmMediaTypesProvider[T] {
      val targetMediaType = self.targetMediaType
      val sourceMediaTypes = self.sourceMediaTypes
    }

    def getForSerialization(contentFormat: String): AlmValidation[AlmMediaType] = {
      if (self.targetMediaType.contentFormat == contentFormat)
        self.targetMediaType.success
      else
        NoSuchElementProblem(s"""No marshallable media type has a content format "$contentFormat".""").failure
    }

    def findForSerialization(contentFormat: String): Option[AlmMediaType] =
      getForSerialization(contentFormat).fold(
        _ ⇒ None,
        succ ⇒ Some(succ))

    def getForSerialization(mainType: String, contentFormat: String): AlmValidation[AlmMediaType] = {
      if (self.targetMediaType.contentFormat == contentFormat && self.targetMediaType.mainType == mainType)
        self.targetMediaType.success
      else
        NoSuchElementProblem(s"""No media type in media range "$mainType" has a content format "$contentFormat".""").failure
    }

    def findForSerialization(mainType: String, contentFormat: String): Option[AlmMediaType] =
      getForSerialization(mainType, contentFormat).fold(
        _ ⇒ None,
        succ ⇒ Some(succ))

    def getForDeserialization(contentFormat: String): AlmValidation[AlmMediaType] = {
      self.sourceMediaTypes.find(_.contentFormat == contentFormat) match {
        case Some(mt) ⇒ mt.success
        case None     ⇒ NoSuchElementProblem(s"""No unmarshallable media type has a content format "$contentFormat".""").failure
      }
    }

    def getAppForSerialization(contentFormat: String) = getForSerialization("application", contentFormat)
    def findAppForSerialization(contentFormat: String) = findForSerialization("application", contentFormat)

    def defaultForSerialization: Option[AlmMediaType] = Option(self.targetMediaType)
    def defaultForDeserialization: Option[AlmMediaType] = self.sourceMediaTypes.headOption
  }

  def createRegisteredDefaults(vendor: MediaTypeVendorPart, content: String): Seq[AlmMediaType] =
    Seq(AlmMediaTypes.registeredMessagePackStructuredMedia(vendor, content),
      AlmMediaTypes.registeredJsonStructuredMedia(vendor, content),
      AlmMediaTypes.registeredXmlStructuredMedia(vendor, content))

  val genericSerializationAppendix = Seq(
    AlmMediaTypes.`text/html`,
    AlmMediaTypes.`application/x-msgpack`,
    AlmMediaTypes.`application/json`,
    AlmMediaTypes.`application/xml`,
    AlmMediaTypes.`text/json`,
    AlmMediaTypes.`text/xml`)
}