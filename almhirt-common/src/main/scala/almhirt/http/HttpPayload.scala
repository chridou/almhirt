package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

sealed trait HttpPayload { def data: Any }
final case class BinaryPayload(data: Array[Byte]) extends HttpPayload
final case class TextPayload(data: String) extends HttpPayload

