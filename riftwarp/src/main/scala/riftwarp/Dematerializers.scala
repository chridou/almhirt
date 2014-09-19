package riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.DematerializersRegistry

trait Dematerializers {
  def add[T](dematerializer: Dematerializer[T])
  def get(channel: String): AlmValidation[Dematerializer[Any]]

}

object Dematerializers {
  def apply(): Dematerializers = DematerializersRegistry()
  def empty(): Dematerializers = DematerializersRegistry.empty
  
  
  implicit class DematerializersOps(self: Dematerializers) {
    def dematerialize(channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[Any] = {
      self.get(channel).map(f ⇒ f(what, options))
    }

    def dematerializeTyped[T](channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty)(implicit tag: ClassTag[T]): AlmValidation[T] = {
      for {
        dematerializer <- self.get(channel)
        typed <- dematerializer(what, options).castTo[T]
      } yield typed
    }
  }
}