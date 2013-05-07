package riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.DematerializersRegistry

trait Dematerializers {
  def add[T](dematerializer: Dematerializer[T])
  def get(dimension: String, channel: String): AlmValidation[Dematerializer[Any]]

}

object Dematerializers {
  def apply(): Dematerializers = DematerializersRegistry()
  def empty(): Dematerializers = DematerializersRegistry.empty
  
  
  implicit class DematerializersOps(self: Dematerializers) {
    def dematerialize(warpStreamName: String, channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[Any] = {
      self.get(warpStreamName, channel).map(f => f(what, options))
    }

    def dematerializeTyped[T](warpStreamName: String, channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty)(implicit tag: ClassTag[T]): AlmValidation[T] = {
      for {
        dematerializer <- self.get(warpStreamName, channel)
        typed <- dematerializer(what, options).castTo[T]
      } yield typed
    }
  }
}