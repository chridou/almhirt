package riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.RematerializersRegistry

trait Rematerializers {
  def add[T](rematerializer: Rematerializer[T])
  def get(channel: String): AlmValidation[Rematerializer[_]]
}

object Rematerializers {
  def apply(): Rematerializers = RematerializersRegistry()
  def empty(): Rematerializers = RematerializersRegistry.empty

  implicit class RematerializersOps(self: Rematerializers) {
//    def addTyped[T](channel: String, rematerialize: (T, Map[String, Any]) => AlmValidation[WarpPackage])(implicit tag: ClassTag[T]) =
//      addByClass(
//          tag.runtimeClass, channel, 
//          (dim: Any, options: Map[String, Any]) => dim.castTo[T].flatMap(tDim => rematerialize(tDim, options)))
//          
//    def addByClass(dimensionType: Class[_], channel: String, rematerialize: (Any, Map[String, Any]) => AlmValidation[WarpPackage]) =
//      self.add(dimensionType.getName(), channel, rematerialize)
//
//    def getByClass(dimensionType: Class[_], channel: String): AlmValidation[(Any, Map[String, Any]) => AlmValidation[WarpPackage]] =
//      self.get(dimensionType.getName(), channel)
    
    def getTyped[T](channel: String): AlmValidation[Rematerializer[T]] = {
      self.get(channel).map(_.asInstanceOf[Rematerializer[T]])
    }
      
    def rematerialize(channel: String, from: Any, options: Map[String, Any]): AlmValidation[WarpPackage] = {
      self.getTyped[Any](channel).flatMap(_.rematerialize(from, options))
    }

    def rematerializeTyped[T](channel: String, from: T, options: Map[String, Any])(implicit tag: ClassTag[T]): AlmValidation[WarpPackage] = {
      for {
        rematerializer <- self.getTyped[T](channel)
        rematerialized <- rematerializer.rematerialize(from, options)
      } yield rematerialized
    }
  }
  
}