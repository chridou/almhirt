package riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.RematerializersRegistry

trait Rematerializers {
  def add(dimension: String, channel: String, rematerialize: (Any, Map[String, Any]) => AlmValidation[WarpPackage])
  def get(dimension: String, channel: String): AlmValidation[(Any, Map[String, Any]) => AlmValidation[WarpPackage]]
}

object Rematerializers {
  def apply(): Rematerializers = RematerializersRegistry()
  def empty(): Rematerializers = RematerializersRegistry.empty

  implicit class RematerializersOps(self: Rematerializers) {
    def addTyped[T](channel: String, rematerialize: (T, Map[String, Any]) => AlmValidation[WarpPackage])(implicit tag: ClassTag[T]) =
      addByClass(
          tag.runtimeClass, channel, 
          (dim: Any, options: Map[String, Any]) => dim.castTo[T].flatMap(tDim => rematerialize(tDim, options)))
          
    def addByClass(dimensionType: Class[_], channel: String, rematerialize: (Any, Map[String, Any]) => AlmValidation[WarpPackage]) =
      self.add(dimensionType.getName(), channel, rematerialize)

    def getByClass(dimensionType: Class[_], channel: String): AlmValidation[(Any, Map[String, Any]) => AlmValidation[WarpPackage]] =
      self.get(dimensionType.getName(), channel)
    
    def getTyped[T](channel: String)(implicit tag: ClassTag[T]): AlmValidation[(T, Map[String, Any]) => AlmValidation[WarpPackage]] =
      self.getByClass(tag.runtimeClass, channel).map(rematerialize => 
        (from: Any, options: Map[String, Any]) => from.castTo[T].flatMap(tDim => rematerialize(tDim, options)))
      
    def rematerialize(warpStreamName: String, channel: String, from: Any, options: Map[String, Any]): AlmValidation[WarpPackage] = {
      self.get(warpStreamName, channel).flatMap(f => f(from, options))
    }

    def rematerializeTyped[T](channel: String, from: T, options: Map[String, Any])(implicit tag: ClassTag[T]): AlmValidation[WarpPackage] = {
      for {
        rematerialize <- self.getTyped[T](channel)
        rematerialized <- rematerialize(from, options)
      } yield rematerialized
    }
  }
  
}