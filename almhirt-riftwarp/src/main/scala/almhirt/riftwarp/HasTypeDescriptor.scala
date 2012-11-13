package almhirt.riftwarp

trait HasTypeDescriptor {
  def typeDescriptor: TypeDescriptor
}

trait HasDefaultDescriptor extends HasTypeDescriptor {
  val typeDescriptor = TypeDescriptor(this.getClass().getName())
}