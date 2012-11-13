package almhirt.riftwarp

case class TypeDescriptor(descriptor: String)

object TypeDescriptor {
  def apply(clazz: Class[_]): TypeDescriptor = TypeDescriptor(clazz.getName())
}