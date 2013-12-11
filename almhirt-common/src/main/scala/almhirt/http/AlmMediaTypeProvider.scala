package almhirt.http

trait AlmMediaTypeProvider[T] {
  def marshallingMediaTypes: Seq[AlmMediaType]
  def unmarshallingMediaTypes: Seq[AlmMediaType]
}