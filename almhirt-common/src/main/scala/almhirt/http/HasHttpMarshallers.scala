package almhirt.http

import scala.reflect.ClassTag
import almhirt.common.AlmValidation

trait HasHttpMarshallers {
  def getMarschaller[T](implicit tag: ClassTag[T]): AlmValidation[HttpMarshaller[T]]
}