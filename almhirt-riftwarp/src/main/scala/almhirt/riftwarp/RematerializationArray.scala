package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

/** Extracts atoms from the other side */
trait RematerializationArray {
  def getString(ident: String): AlmValidation[String]
  def tryGetString(ident: String): AlmValidation[Option[String]]

  def getBoolean(ident: String): AlmValidation[Boolean]
  def tryGetBoolean(ident: String): AlmValidation[Option[Boolean]]

  def getByte(ident: String): AlmValidation[Byte]
  def tryGetByte(ident: String): AlmValidation[Option[Byte]]
  def getInt(ident: String): AlmValidation[Int]
  def tryGetInt(ident: String): AlmValidation[Option[Int]]
  def getLong(ident: String): AlmValidation[Long]
  def tryGetLong(ident: String): AlmValidation[Option[Long]]
  def getBigInt(ident: String): AlmValidation[BigInt]
  def tryGetBigInt(ident: String): AlmValidation[Option[BigInt]]
  
  def getFloat(ident: String): AlmValidation[Float]
  def tryGetFloat(ident: String): AlmValidation[Option[Float]]
  def getDouble(ident: String): AlmValidation[Double]
  def tryGetDouble(ident: String): AlmValidation[Option[Double]]
  def getBigDecimal(ident: String): AlmValidation[BigDecimal]
  def tryGetBigDecimal(ident: String): AlmValidation[Option[BigDecimal]]
  
  def getByteArray(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArray(ident: String): AlmValidation[Option[Array[Byte]]]
  def getBlob(ident: String): AlmValidation[Array[Byte]]
  def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]]

  def getDateTime(ident: String): AlmValidation[org.joda.time.DateTime]
  def tryGetDateTime(ident: String): AlmValidation[Option[org.joda.time.DateTime]]
  
  def getUuid(ident: String): AlmValidation[_root_.java.util.UUID]
  def tryGetUuid(ident: String): AlmValidation[Option[_root_.java.util.UUID]]

  def getJson(ident: String): AlmValidation[String]
  def tryGetJson(ident: String): AlmValidation[Option[String]]
  def getXml(ident: String): AlmValidation[scala.xml.Node]
  def tryGetXml(ident: String): AlmValidation[Option[scala.xml.Node]]

//  def getComplexType[T](ident: String, rec: Recomposer[T]): AlmValidationMBD[T]
//  def tryGetComplexType[T](ident: String, rec: Recomposer[T]): AlmValidationMBD[Option[T]]
  
  def getTypeDescriptor: AlmValidation[TypeDescriptor]
  def tryGetTypeDescriptor: AlmValidation[Option[TypeDescriptor]]
}

trait RematiarializationArrayBasedOnOptionGetters extends RematerializationArray {
  def getString(ident: String) = tryGetString(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getBoolean(ident: String) = tryGetBoolean(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getByte(ident: String) = tryGetByte(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getInt(ident: String) = tryGetInt(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getLong(ident: String) = tryGetLong(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigInt(ident: String) = tryGetBigInt(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  
  def getFloat(ident: String) = tryGetFloat(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getDouble(ident: String) = tryGetDouble(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigDecimal(ident: String) = tryGetBigDecimal(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  
  def getByteArray(ident: String) = tryGetByteArray(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBlob(ident: String) = tryGetBlob(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getDateTime(ident: String) = tryGetDateTime(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  
  def getUuid(ident: String) = tryGetUuid(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getJson(ident: String) = tryGetJson(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getXml(ident: String) = tryGetXml(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getTypeDescriptor = tryGetTypeDescriptor.bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(TypeDescriptor.defaultKey), args = Map("key" -> TypeDescriptor.defaultKey)).failure))

}