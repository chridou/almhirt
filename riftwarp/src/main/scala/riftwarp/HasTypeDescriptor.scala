package riftwarp

trait HasTypeDescriptor {
  def typeDescriptor: TypeDescriptor
}

trait HasDefaultTypeDescriptor extends HasTypeDescriptor {
  val typeDescriptor = TypeDescriptor(this.getClass().getName())
}