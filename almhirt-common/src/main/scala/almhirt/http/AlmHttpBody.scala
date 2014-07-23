package almhirt.http

sealed trait AlmHttpBody {
  def value: Any
}

final case class BinaryBody(value: Array[Byte]) extends AlmHttpBody
final case class TextBody(value: String) extends AlmHttpBody
