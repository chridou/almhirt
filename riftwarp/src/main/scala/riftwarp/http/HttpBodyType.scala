package riftwarp.http

sealed trait HttpBodyType
object BinaryBodyType extends HttpBodyType
object StringBodyType extends HttpBodyType
