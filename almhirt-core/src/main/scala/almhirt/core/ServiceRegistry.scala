package almhirt.core

import almhirt.common._

trait HasServices {
  def getService[T <: AnyRef](implicit m: Manifest[T]): AlmValidation[T] =
    getServiceByType(m.runtimeClass.asInstanceOf[Class[_ <: AnyRef]]).map(_.asInstanceOf[T])

  def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef]
}

trait CanRegisterServices {
  def registerService[TService <: AnyRef](serviceImpl: TService)(implicit mService: Manifest[TService]) = registerServiceByType(mService.runtimeClass.asInstanceOf[Class[_ <: AnyRef]], serviceImpl)
  def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef)
}

trait ServiceRegistry extends HasServices with CanRegisterServices