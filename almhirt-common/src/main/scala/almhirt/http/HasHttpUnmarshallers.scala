package almhirt.http

import scala.reflect.ClassTag
import almhirt.common.AlmValidation

trait HasHttpUnmarshallers {
  def getUnmarschaller[T](implicit tag: ClassTag[T]): AlmValidation[HttpUnmarshaller[T]]
}