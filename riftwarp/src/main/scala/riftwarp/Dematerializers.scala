package riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.DematerializersRegistry

trait Dematerializers {
  def add(dimension: String, channel: String, dematerialize: (WarpPackage, Map[String, Any]) => Any)
  def get(dimension: String, channel: String): AlmValidation[(WarpPackage, Map[String, Any]) => Any]

}

object Dematerializers {
  def apply(): Dematerializers = DematerializersRegistry()
  def empty(): Dematerializers = DematerializersRegistry.empty
  
  
  implicit class DematerializersOps(self: Dematerializers) {
    def addTyped[T](channel: String, dematerialize: (WarpPackage, Map[String, Any]) => T)(implicit tag: ClassTag[T]) =
      addByClass(tag.runtimeClass, channel, dematerialize)
    def addByClass(dimensionType: Class[_], channel: String, dematerialize: (WarpPackage, Map[String, Any]) => Any) =
      self.add(dimensionType.getName(), channel, dematerialize)

    def dematerialize(warpStreamName: String, channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[Any] = {
      self.get(warpStreamName, channel).map(f => f(what, options))
    }

    def dematerializeTyped[T](warpStreamName: String, channel: String, what: WarpPackage, options: Map[String, Any] = Map.empty)(implicit tag: ClassTag[T]): AlmValidation[T] = {
      for {
        dematerialize <- self.get(warpStreamName, channel)
        typed <- dematerialize(what, options).castTo[T]
      } yield typed
    }
  }
}