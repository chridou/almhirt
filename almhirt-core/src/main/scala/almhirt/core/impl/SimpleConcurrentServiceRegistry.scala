package almhirt.core.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.ServiceRegistry

class SimpleConcurrentServiceRegistry extends ServiceRegistry {
  private val registeredServices = new java.util.concurrent.ConcurrentHashMap[Class[_ <: AnyRef], AnyRef](128)

  def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef) {
    registeredServices.put(clazz, service)
  }

  def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef] =
    registeredServices.get(clazz) match {
      case null => ServiceNotFoundProblem("No implementation found for service '%s'".format(clazz.getName())).failure
      case service => service.success
    }

}