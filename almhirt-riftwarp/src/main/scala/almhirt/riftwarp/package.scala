package almhirt

import almhirt.riftwarp.impl.dematerializers.simplema.ToJsonCordPrimitiveMAs

package object riftwarp extends ToJsonCordPrimitiveMAs {
  implicit def string2TypeDescriptor(descriptor: String): TypeDescriptor = TypeDescriptor(descriptor)
  implicit def class2TypeDescriptor(clazz: Class[_]): TypeDescriptor = TypeDescriptor(clazz.getName())
  implicit def uuid2TypeDescriptor(uuid: java.util.UUID): TypeDescriptor = TypeDescriptor(uuid.toString())
  implicit def long2TypeDescriptor(id: Long): TypeDescriptor = TypeDescriptor(id.toString())
  
  implicit def string2DimensionString(str: String): DimensionString = DimensionString(str)
  implicit def cord2DimensionCord(cord: scalaz.Cord): DimensionCord = DimensionCord(cord)
  implicit def arrayByte2DimensionBinary(array: Array[Byte]): DimensionBinary = DimensionBinary(array)
}