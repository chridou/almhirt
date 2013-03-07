package riftwarp

import almhirt.common._
import scala.reflect.ClassTag
import riftwarp.components.HasRecomposers

trait RRematerializer[TDimension <: RiftDimension] {
  type ValueRepr = TDimension#Under
  
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

  def getWithFromRepr[T](value: ValueRepr, recomposes: Recomposes[T]): AlmValidation[T]
  def getComplexByDescriptorFromRepr(value: ValueRepr, riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[Any]
  def getComplexWithTagFromRepr[T](value: ValueRepr, backupRiftDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[T]

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

  def getWith[T](from: TDimension, recomposes: Recomposes[T]): AlmValidation[T]
  def getComplexByDescriptor(from: TDimension, riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[Any]
  def getComplexWithTag[T](from: TDimension, backupRiftDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[T]
  
}