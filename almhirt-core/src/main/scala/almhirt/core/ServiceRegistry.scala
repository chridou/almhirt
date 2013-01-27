package almhirt.core

import scala.reflect.ClassTag
import almhirt.common._
import com.typesafe.config.Config

trait HasServices {
  def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef]
}

object HasServices {
  implicit class HasServicesOps(self: HasServices) {
    def getService[T <: AnyRef](implicit m: ClassTag[T]): AlmValidation[T] =
      self.getServiceByType(m.runtimeClass.asInstanceOf[Class[_ <: AnyRef]]).flatMap(almhirt.almvalidation.funs.almCast[T](_))
      
    def getConfig: AlmValidation[Config] = getService[HasConfig].map(_.config)
  }
}

trait CanRegisterServices {
  def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef)
}

object CanRegisterServices {
  implicit class CanRegisterServicesOps(self: CanRegisterServices) {
    def registerService[TService <: AnyRef](serviceImpl: TService)(implicit mService: ClassTag[TService]) = self.registerServiceByType(mService.runtimeClass.asInstanceOf[Class[_ <: AnyRef]], serviceImpl)
  }
}

trait ServiceRegistry extends HasServices with CanRegisterServices