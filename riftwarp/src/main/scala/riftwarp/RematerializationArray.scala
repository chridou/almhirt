package riftwarp

import language.higherKinds

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
  def getByteArrayFromBase64Encoding(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArrayFromBase64Encoding(ident: String): AlmValidation[Option[Array[Byte]]]
  def getByteArrayFromBlobEncoding(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArrayFromBlobEncoding(ident: String): AlmValidation[Option[Array[Byte]]]

  def getDateTime(ident: String): AlmValidation[org.joda.time.DateTime]
  def tryGetDateTime(ident: String): AlmValidation[Option[org.joda.time.DateTime]]

  def getUri(ident: String): AlmValidation[_root_.java.net.URI]
  def tryGetUri(ident: String): AlmValidation[Option[_root_.java.net.URI]]

  def getUuid(ident: String): AlmValidation[_root_.java.util.UUID]
  def tryGetUuid(ident: String): AlmValidation[Option[_root_.java.util.UUID]]

   
  def getJson(ident: String): AlmValidation[String]
  def tryGetJson(ident: String): AlmValidation[Option[String]]
  def getXml(ident: String): AlmValidation[scala.xml.Node]
  def tryGetXml(ident: String): AlmValidation[Option[scala.xml.Node]]

  def getBlob(ident: String): AlmValidation[Array[Byte]]
  def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]]
  
  /** Rematerialize the complex type given a recomposer
   */
  def getComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]): AlmValidation[T]
  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]): AlmValidation[Option[T]]
  /** Rematerialize the complex type and search for a recomposer
   * 1) Check whether there is a typedescriptor in the dematerialized data
   * 2) Use the type of T's erasure
   */
  def getComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[T]
  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]]
  /** Rematerialize the complex type and search for a recomposer
   * The recomposer is searched using T's erasure
   */
  def getComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[T]
  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]]

  /** Rematerialize an M[_] of primitive types
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  /** Rematerialize an M[_] of complex types
   * 
   * Complex types are all types that are not String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime or UUID */
  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  /** Rematerialize an M[_] of complex types using the given recomposer
   * The recomposer is used for all elements of M
   * 
   * Complex types are all types that are not String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime or UUID */
  def getComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  /** Rematerialize an M[_] of complex types. The decomposer is searched by T's erasure
   * The recomposer is used for all elements of M
   * 
   * Complex types are all types that are not String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime or UUID */
  def getComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]
  /** Rematerialize an M[_] of complex types. 
   * For each element of M search a recomposer by using its typedescriptor
   * 
   * Complex types are all types that are not String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime or UUID */
  def getComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]]): AlmValidation[M[A]]
  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]]): AlmValidation[Option[M[A]]]
  /** Rematerialize an M[_] of any types. 
   * Rematerializes a primitive type or looks up a recomposer by the elements typedescriptor
   * 
   * Complex types are all types that are not String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime or UUID */
  def getMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[M[A]]
  def tryGetMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]]

  /** Rematerialize a Map[A,B] where both A and B are primitive types
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getPrimitiveMap[A,B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Map[A, B]]
  def tryGetPrimitiveMap[A,B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]]
  /** Rematerialize a Map[A,B] where A is a primitive type and B is a complex type.
   * Use the given recomposer to rematerialize Bs.
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getComplexMap[A,B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: Manifest[A]): AlmValidation[Map[A, B]]
  def tryGetComplexMap[A,B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]]
  /** Rematerialize a Map[A,B] where A is a primitive type and B is a complex type.
   * Use the erasure of B to find a recomposer for all elements.
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getComplexMapFixed[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Map[A, B]]
  def tryGetComplexMapFixed[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]]
  /** Rematerialize a Map[A,B] where A is a primitive type and B is a complex type.
   * Lookup a typedescriptor for each element and find a suiting recomposer.
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getComplexMapLoose[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A]): AlmValidation[Map[A, B]]
  def tryGetComplexMapLoose[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]]
  /** Rematerialize a Map[A,B] where A is a primitive type and B can be anything.
   * Lookup a primitive rematerializer or a recomposer by the elements typedescriptor for each element.
   * 
   * Primitive types are  String, Boolean, Byte, Int, Long, BigInt, Float, Double, BigDecimal, DateTime and UUID */
  def getMap[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Map[A, B]]
  def tryGetMap[A,B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]]
  
  def getTypeDescriptor: AlmValidation[TypeDescriptor]
  def tryGetTypeDescriptor: AlmValidation[Option[TypeDescriptor]]
}

abstract class RematerializationArrayWithBlobBlobFetch extends RematerializationArray {
  protected def fetchBlobData: BlobFetch
  protected def trySpawnNew(ident: String): AlmValidation[Option[RematerializationArray]]
  protected def tryGetRematerializedBlob(ident: String): AlmValidation[Option[Array[Byte]]] =
    trySpawnNew(ident).flatMap(rematOpt =>
      option.cata(rematOpt)(
       remat => RiftBlob.recompose(remat).flatMap(theBlob =>
        fetchBlobData(theBlob).map(Some(_))),
        None.success))
}

trait RematerializationArrayBasedOnOptionGetters extends RematerializationArray {
  def getString(ident: String) = tryGetString(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getBoolean(ident: String) = tryGetBoolean(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getByte(ident: String) = tryGetByte(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getInt(ident: String) = tryGetInt(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getLong(ident: String) = tryGetLong(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigInt(ident: String) = tryGetBigInt(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getFloat(ident: String) = tryGetFloat(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getDouble(ident: String) = tryGetDouble(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getBigDecimal(ident: String) = tryGetBigDecimal(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getByteArray(ident: String) = tryGetByteArray(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getByteArrayFromBase64Encoding(ident: String) = tryGetByteArrayFromBase64Encoding(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getByteArrayFromBlobEncoding(ident: String) = tryGetByteArrayFromBlobEncoding(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getDateTime(ident: String) = tryGetDateTime(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getUri(ident: String) = tryGetUri(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getUuid(ident: String) = tryGetUuid(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getJson(ident: String) = tryGetJson(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getXml(ident: String) = tryGetXml(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getBlob(ident: String) = tryGetBlob(ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  
  def getComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) = tryGetComplexType[T](ident, recomposer).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]) = tryGetComplexType[T](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]) = tryGetComplexTypeFixed[T](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetPrimitiveMA[M, A](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetComplexMA[M, A](ident, recomposer).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetComplexMAFixed[M, A](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]]) = tryGetComplexMALoose[M, A](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]) = tryGetMA[M, A](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))

  def getPrimitiveMap[A,B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]) = tryGetPrimitiveMap[A, B](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMap[A,B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: Manifest[A]) = tryGetComplexMap[A, B](ident, recomposer).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMapFixed[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]) = tryGetComplexMapFixed[A, B](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getComplexMapLoose[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A]) = tryGetComplexMapLoose[A, B](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  def getMap[A,B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]) = tryGetMap[A, B](ident).flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(ident), args = Map("key" -> ident)).failure))
  
  def getTypeDescriptor = tryGetTypeDescriptor.flatMap(v => option.cata(v)(_.success, KeyNotFoundProblem("Nothing found for '%s'".format(TypeDescriptor.defaultKey), args = Map("key" -> TypeDescriptor.defaultKey)).failure))
}