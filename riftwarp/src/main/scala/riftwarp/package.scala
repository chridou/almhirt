import scalaz.syntax.validation._
import almhirt.common._

package object riftwarp {
  import language.implicitConversions
  
  implicit def string2RiftDescriptor(descriptor: String): RiftDescriptor = RiftDescriptor(descriptor)
  implicit def class2RiftDescriptor(clazz: Class[_]): RiftDescriptor = RiftDescriptor(clazz.getName())
  implicit def uuid2RiftDescriptor(uuid: java.util.UUID): RiftDescriptor = RiftDescriptor(uuid.toString())
  implicit def long2RiftDescriptor(id: Long): RiftDescriptor = RiftDescriptor(id.toString())
  
//  implicit def string2DimensionString(str: String): DimensionString = DimensionString(str)
//  implicit def cord2DimensionCord(cord: scalaz.Cord): DimensionCord = DimensionCord(cord)
//  implicit def arrayByte2DimensionBinary(array: Array[Byte]): DimensionBinary = DimensionBinary(array)
  
  object funs {
    import riftwarp.components._
    object hasRecomposers extends HasRecomposersFuns 
  }
  
  object inst extends CanBuildFroms
}