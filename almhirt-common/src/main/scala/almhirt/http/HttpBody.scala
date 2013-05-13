package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

sealed trait HttpBody { def data: Any }
final case class BinaryBody(data: Array[Byte]) extends HttpBody
final case class TextBody(data: String) extends HttpBody

