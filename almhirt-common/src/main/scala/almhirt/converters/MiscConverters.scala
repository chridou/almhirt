package almhirt.converters

import java.nio.ByteBuffer

object MiscConverters {
  private val BASE64 = new org.apache.commons.codec.binary.Base64(true)
  @inline
  def uuidToBase64String(uuid: java.util.UUID): String = {
    val b64Str = new String(BASE64.encode(almhirt.converters.BinaryConverter.uuidToBytes(uuid)))
    b64Str.substring(0, b64Str.length() - 2)
  }

  @inline
  def base64StringToUuid(str: String): almhirt.common.AlmValidation[java.util.UUID] = {
    try {
      val bytes = BASE64.decode(str)
      val bb = ByteBuffer.wrap(bytes);
      scalaz.Success(new java.util.UUID(bb.getLong(), bb.getLong()))
    } catch {
      case scala.util.control.NonFatal(ex) ⇒
        scalaz.Failure(almhirt.common.ParsingProblem(s""""$str" is not a base64 encoded UUID.""", cause = Some(ex)))
    }
  }

  @inline
  def uuidStrToBase64Str(str: String): almhirt.common.AlmValidation[String] =
    almhirt.almvalidation.funs.parseUuidAlm(str).map(uuidToBase64String)

  @inline
  def uuidBase64StrToUuidString(str: String): almhirt.common.AlmValidation[String] =
    base64StringToUuid(str).map(_.toString())

}