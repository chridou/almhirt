package riftwarp

import language.higherKinds

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import almhirt.common._
import riftwarp.components.HasRecomposers

trait Rematerializer[TDimension <: RiftDimension] {
  type ValueRepr = TDimension#Under

  def valueMapperFromTag[T](implicit tag: ClassTag[T]): AlmValidation[ValueRepr => AlmValidation[T]]
  def primitiveFromValue(value: ValueRepr): AlmValidation[Any]
  def isPrimitive(value: ValueRepr): Boolean

  def stringFromRepr(value: ValueRepr): AlmValidation[String]
  def booleanFromRepr(value: ValueRepr): AlmValidation[Boolean]
  def byteFromRepr(value: ValueRepr): AlmValidation[Byte]
  def intFromRepr(value: ValueRepr): AlmValidation[Int]
  def longFromRepr(value: ValueRepr): AlmValidation[Long]
  def bigIntFromRepr(value: ValueRepr): AlmValidation[BigInt]
  def floatFromRepr(value: ValueRepr): AlmValidation[Float]
  def doubleFromRepr(value: ValueRepr): AlmValidation[Double]
  def bigDecimalFromRepr(value: ValueRepr): AlmValidation[BigDecimal]
  def byteArrayFromRepr(value: ValueRepr): AlmValidation[Array[Byte]]
  def byteArrayFromBase64Repr(value: ValueRepr): AlmValidation[Array[Byte]]
  def byteArrayFromBlobRepr(value: ValueRepr): AlmValidation[Array[Byte]]
  def dateTimeFromRepr(value: ValueRepr): AlmValidation[org.joda.time.DateTime]
  def uriFromRepr(value: ValueRepr): AlmValidation[_root_.java.net.URI]
  def uuidFromRepr(value: ValueRepr): AlmValidation[_root_.java.util.UUID]

  def traversableOfReprFromRepr(value: ValueRepr): AlmValidation[Traversable[ValueRepr]]
  def tuple2OfReprFromRepr(value: ValueRepr): AlmValidation[(ValueRepr, ValueRepr)]

  def traversable2FromRepr(value: ValueRepr): AlmValidation[Traversable[(ValueRepr, ValueRepr)]]
  def collectionOfReprFromRepr[That[_]](value: ValueRepr)(implicit cbf: CanBuildFrom[Traversable[_], ValueRepr, That[ValueRepr]]): AlmValidation[That[ValueRepr]]

  def fromRepr[T](value: ValueRepr, f: Extractor => AlmValidation[T], createExtractor: ValueRepr => AlmValidation[Extractor]): AlmValidation[T]

  def resequencedMappedFromRepr[That[_], T](value: ValueRepr, f: ValueRepr => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def resequencedOfPrimitivesFromRepr[That[_], T](value: ValueRepr)(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def retuplelized2MappedFromRepr[A, B](value: ValueRepr, fa: ValueRepr => AlmValidation[A], fb: ValueRepr => AlmValidation[B]): AlmValidation[(A, B)]
  def retuplelized2TraversableMappedFromRepr[A, B](value: ValueRepr, fa: ValueRepr => AlmValidation[A], fb: ValueRepr => AlmValidation[B]): AlmValidation[Traversable[(A, B)]]

  def getString(from: TDimension): AlmValidation[String]
  def getBoolean(from: TDimension): AlmValidation[Boolean]
  def getByte(from: TDimension): AlmValidation[Byte]
  def getInt(from: TDimension): AlmValidation[Int]
  def getLong(from: TDimension): AlmValidation[Long]
  def getBigInt(from: TDimension): AlmValidation[BigInt]
  def getFloat(from: TDimension): AlmValidation[Float]
  def getDouble(from: TDimension): AlmValidation[Double]
  def getBigDecimal(from: TDimension): AlmValidation[BigDecimal]
  def getByteArray(from: TDimension): AlmValidation[Array[Byte]]
  def getByteArrayFromBase64Encoding(from: TDimension): AlmValidation[Array[Byte]]
  def getByteArrayFromBlobEncoding(from: TDimension): AlmValidation[Array[Byte]]
  def getDateTime(from: TDimension): AlmValidation[org.joda.time.DateTime]
  def getUri(from: TDimension): AlmValidation[_root_.java.net.URI]
  def getUuid(from: TDimension): AlmValidation[_root_.java.util.UUID]

}