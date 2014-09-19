package riftwarp.std

import scalaz.syntax.validation._
import almhirt.common._

object ParseFuns {
  def parseBase64Alm(toParse: String): AlmValidation[Array[Byte]] =
    try {
      org.apache.commons.codec.binary.Base64.decodeBase64(toParse).success
    } catch {
      case err: Exception ⇒ BadDataProblem("Not a Base64 encoded String".format(toParse)).failure[Array[Byte]]
    }

  def tryParseBase64Alm(toParse: String): AlmValidation[Option[Array[Byte]]] =
    if (toParse.trim().isEmpty())
      None.success
    else
      parseBase64Alm(toParse).map(Some(_))
      
}