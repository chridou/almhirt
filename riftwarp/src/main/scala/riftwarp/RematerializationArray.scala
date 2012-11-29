package riftwarp

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

  def getComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]): AlmValidation[T]
  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]): AlmValidation[Option[T]]
  def getComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[T]
  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]]

  def getPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  def getComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  def getComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  def getComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]

  def getTypeDescriptor: AlmValidation[TypeDescriptor]
  def tryGetTypeDescriptor: AlmValidation[Option[TypeDescriptor]]
}

trait RematerializationArrayBasedOnOptionGetters extends RematerializationArray {
  def getString(ident: String) = tryGetString(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getBoolean(ident: String) = tryGetBoolean(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getByte(ident: String) = tryGetByte(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getInt(ident: String) = tryGetInt(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getLong(ident: String) = tryGetLong(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigInt(ident: String) = tryGetBigInt(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getFloat(ident: String) = tryGetFloat(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getDouble(ident: String) = tryGetDouble(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigDecimal(ident: String) = tryGetBigDecimal(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getByteArray(ident: String) = tryGetByteArray(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBlob(ident: String) = tryGetBlob(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getDateTime(ident: String) = tryGetDateTime(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getUuid(ident: String) = tryGetUuid(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getJson(ident: String) = tryGetJson(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getXml(ident: String) = tryGetXml(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) = tryGetComplexType(ident, recomposer).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]) = tryGetComplexType(ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetPrimitiveMA[M, A](ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetComplexMA[M, A](ident, recomposer).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetComplexMAFixed[M, A](ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetComplexMALoose[M, A](ident).bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
 
  def getTypeDescriptor = tryGetTypeDescriptor.bind(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(TypeDescriptor.defaultKey), args = Map("key" -> TypeDescriptor.defaultKey)).failure))
}