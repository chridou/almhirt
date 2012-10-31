package almhirt

trait HasServices {
  def getService[T <: AnyRef]: AlmFuture[T]
  def awaitService[T <: AnyRef]: AlmValidation[T]
}

trait RegistersServices {
  def registerService[T <: AnyRef](service: T)(implicit m: Manifest[T]) = registerServiceByType(m.erasure, service)
  def registerServiceByType(clazz: Class[_], service: AnyRef)
}