package almhirt.environment

import scalaz.std._
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import almhirt.messaging._
import org.joda.time.DateTime

trait Almhirt extends AlmhirtBaseOps with CreatesMessageChannels with HasServices {
  def system: AlmhirtSystem
  def serviceRegistry: Option[ServiceRegistry]
  def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef] =
    option.cata(serviceRegistry)(
      sr => sr.getServiceByType(clazz),
      scalaz.Failure(ServiceNotFoundProblem("There is no service registry. Maybe serviceRegistry == None?!")))
  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = {
    val header = MessageHeader(system.getUuid, None, Map.empty, system.getDateTime)
    Message(header, payload)
  }
}