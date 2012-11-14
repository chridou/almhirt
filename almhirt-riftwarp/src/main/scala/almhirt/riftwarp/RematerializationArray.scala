package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

/** Extracts atoms from the other side */
trait RematerializationArray {
  def getString(ident: String): AlmValidation[String]
  def tryGetString(ident: String): AlmValidation[Option[String]]

//  def getBoolean(ident: String): AlmValidationSBD[String]
//  def tryGetBoolean(ident: String): AlmValidationSBD[Option[String]]

  def getByte(ident: String): AlmValidation[Byte]
  def tryGetByte(ident: String): AlmValidation[Option[Byte]]
  def getInt(ident: String): AlmValidation[Int]
  def tryGetInt(ident: String): AlmValidation[Option[Int]]
//  def getLong(ident: String): AlmValidationSBD[Long]
//  def tryGetLong(ident: String): AlmValidationSBD[Option[Long]]
//  def getBigInt(ident: String): AlmValidationSBD[BigInt]
//  def tryGetBigInt(ident: String): AlmValidationSBD[Option[BigInt]]
//  
//  def getFloat(ident: String): AlmValidationSBD[Float]
//  def tryGetFloat(ident: String): AlmValidationSBD[Option[Float]]
//  def getDouble(ident: String): AlmValidationSBD[Double]
//  def tryGetDouble(ident: String): AlmValidationSBD[Option[Double]]
//  def getBigDecimal(ident: String): AlmValidationSBD[BigDecimal]
//  def tryGetBigDecimal(ident: String): AlmValidationSBD[Option[BigDecimal]]
//  
//  def getByteArray(ident: String): AlmValidationSBD[Array[Byte]]
//  def tryGetByteArray(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//  def getBlob(ident: String): AlmValidationSBD[Array[Byte]]
//  def tryGetBlob(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//
//  def getDateTime(ident: String): AlmValidationSBD[org.joda.time.DateTime]
//  def tryGetDateTime(ident: String): AlmValidationSBD[Option[org.joda.time.DateTime]]
//  
//  def getUuid(ident: String): AlmValidationSBD[java.util.UUID]
//  def tryGetUuid(ident: String): AlmValidationSBD[Option[java.util.UUID]]
//
//  def getJson(ident: String): AlmValidationSBD[String]
//  def tryGetJson(ident: String): AlmValidationSBD[Option[String]]
//  def getXml(ident: String): AlmValidationSBD[scala.xml.Node]
//  def tryGetXml(ident: String): AlmValidationSBD[Option[scala.xml.Node]]
//
//  def getComplexType[T](ident: String, rec: Recomposer[T]): AlmValidationMBD[T]
//  def tryGetComplexType[T](ident: String, rec: Recomposer[T]): AlmValidationMBD[Option[T]]
  
  def getTypeDescriptor: AlmValidation[TypeDescriptor]
}

trait RematiarializationArrayBasedOnOptionGetters extends RematerializationArray {
  def getString(ident: String) = tryGetString(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

//  def getBoolean(ident: String): AlmValidationSBD[String]
//  def tryGetBoolean(ident: String): AlmValidationSBD[Option[String]]

  def getByte(ident: String) = tryGetByte(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getInt(ident: String) = tryGetInt(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
//  def getLong(ident: String): AlmValidationSBD[Long]
//  def tryGetLong(ident: String): AlmValidationSBD[Option[Long]]
//  def getBigInt(ident: String): AlmValidationSBD[BigInt]
//  def tryGetBigInt(ident: String): AlmValidationSBD[Option[BigInt]]
//  
//  def getFloat(ident: String): AlmValidationSBD[Float]
//  def tryGetFloat(ident: String): AlmValidationSBD[Option[Float]]
//  def getDouble(ident: String): AlmValidationSBD[Double]
//  def tryGetDouble(ident: String): AlmValidationSBD[Option[Double]]
//  def getBigDecimal(ident: String): AlmValidationSBD[BigDecimal]
//  def tryGetBigDecimal(ident: String): AlmValidationSBD[Option[BigDecimal]]
//  
//  def getByteArray(ident: String): AlmValidationSBD[Array[Byte]]
//  def tryGetByteArray(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//  def getBlob(ident: String): AlmValidationSBD[Array[Byte]]
//  def tryGetBlob(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//
//  def getDateTime(ident: String): AlmValidationSBD[org.joda.time.DateTime]
//  def tryGetDateTime(ident: String): AlmValidationSBD[Option[org.joda.time.DateTime]]
//  
//  def getUuid(ident: String): AlmValidationSBD[java.util.UUID]
//  def tryGetUuid(ident: String): AlmValidationSBD[Option[java.util.UUID]]
//
//  def getJson(ident: String): AlmValidationSBD[String]
//  def tryGetJson(ident: String): AlmValidationSBD[Option[String]]
//  def getXml(ident: String): AlmValidationSBD[scala.xml.Node]
//  def tryGetXml(ident: String): AlmValidationSBD[Option[scala.xml.Node]]
  
}