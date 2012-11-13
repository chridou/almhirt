package almhirt

package object riftwarp {
  implicit def string2TypeDescriptor(descriptor: String): TypeDescriptor = TypeDescriptor(descriptor)
  implicit def class2TypeDescriptor(clazz: Class[_]): TypeDescriptor = TypeDescriptor(clazz.getName())
  implicit def uuid2TypeDescriptor(uuid: java.util.UUID): TypeDescriptor = TypeDescriptor(uuid.toString())
  implicit def long2TypeDescriptor(id: Long): TypeDescriptor = TypeDescriptor(id.toString())
}