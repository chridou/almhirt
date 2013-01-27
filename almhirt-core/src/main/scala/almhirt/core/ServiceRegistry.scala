package almhirt.core

import scala.reflect.ClassTag
import almhirt.common._

trait HasServices {
  def getService[T <: AnyRef](implicit m: ClassTag[T]): AlmValidation[T] =
    getServiceByType(m.runtimeClass.asInstanceOf[Class[_ <: AnyRef]]).flatMap(almhirt.almvalidation.funs.almCast[T](_))

  def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef]
}

trait CanRegisterServices {
  def registerService[TService <: AnyRef](serviceImpl: TService)(implicit mService: ClassTag[TService]) = registerServiceByType(mService.runtimeClass.asInstanceOf[Class[_ <: AnyRef]], serviceImpl)
  def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef)
}

trait ServiceRegistry extends HasServices with CanRegisterServices