package almhirt.converters

import java.nio.ByteBuffer
import almhirt.common.ParsingProblem

object MiscConverters {
  // url-safe = true
  private val URL_SAFE_BASE64 = new org.apache.commons.codec.binary.Base64(true)
  @inline
  def uuidToBase64String(uuid: java.util.UUID): String = {
    val b64Str = new String(URL_SAFE_BASE64.encode(almhirt.converters.BinaryConverter.uuidToBytes(uuid)))
    b64Str.substring(0, b64Str.length() - 2)
  }

  @inline
  def base64ToUuid(str: String): almhirt.common.AlmValidation[java.util.UUID] = {
    try {
      if (str.length() == 20 || str.length() == 22) {
        val bytes = URL_SAFE_BASE64.decode(str)
        scalaz.Success(almhirt.converters.BinaryConverter.bytesToUuid(bytes))
      } else {
        scalaz.Failure(ParsingProblem(s"""A UUID in a base 64 must have 20 or 22 characters, not ${str.length()} as in "$str"."""))
      }

    } catch {
      case scala.util.control.NonFatal(ex) ⇒
        scalaz.Failure(almhirt.common.ParsingProblem(s""""$str" is not a base64 encoded UUID.""", cause = Some(ex)))
    }
  }

  @inline
  def uuidStringToBase64(str: String): almhirt.common.AlmValidation[String] =
    almhirt.almvalidation.funs.parseUuidAlm(str).map(uuidToBase64String)

  @inline
  def base64ToUuidString(str: String): almhirt.common.AlmValidation[String] =
    base64ToUuid(str).map(_.toString())

}