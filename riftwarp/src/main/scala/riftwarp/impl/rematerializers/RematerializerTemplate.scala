package riftwarp.impl.rematerializers

import scala.reflect.ClassTag
import almhirt.common._
import riftwarp._
import riftwarp.components.HasRecomposers

abstract class RematerializerTemplate[TDimension <: RiftDimension] extends RRematerializer[TDimension]{
  type ValueRepr = TDimension#Under

 
  override def getString(from: TDimension): AlmValidation[String] = stringFromRepr(from.manifestation)
  override def getBoolean(from: TDimension): AlmValidation[Boolean] = booleanFromRepr(from.manifestation)
  override def getByte(from: TDimension): AlmValidation[Byte] = byteFromRepr(from.manifestation)
  override def getInt(from: TDimension): AlmValidation[Int] = intFromRepr(from.manifestation)
  override def getLong(from: TDimension): AlmValidation[Long] = longFromRepr(from.manifestation)
  override def getBigInt(from: TDimension): AlmValidation[BigInt] = bigIntFromRepr(from.manifestation)
  override def getFloat(from: TDimension): AlmValidation[Float] = floatFromRepr(from.manifestation)
  override def getDouble(from: TDimension): AlmValidation[Double] = doubleFromRepr(from.manifestation)
  override def getBigDecimal(from: TDimension): AlmValidation[BigDecimal] = bigDecimalFromRepr(from.manifestation)
  override def getByteArray(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromRepr(from.manifestation)
  override def getByteArrayFromBase64Encoding(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromBase64Repr(from.manifestation)
  override def getByteArrayFromBlobEncoding(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromBlobRepr(from.manifestation)
  override def getDateTime(from: TDimension): AlmValidation[org.joda.time.DateTime] = dateTimeFromRepr(from.manifestation)
  override def getUri(from: TDimension): AlmValidation[_root_.java.net.URI] = uriFromRepr(from.manifestation)
  override def getUuid(from: TDimension): AlmValidation[_root_.java.util.UUID] = uuidFromRepr(from.manifestation)

  override def getWith[T](from: TDimension, recomposes: Recomposes[T]): AlmValidation[T] = 
    fromRepr[T](from.manifestation, recomposes: Recomposes[T])
  override def getComplexByDescriptor(from: TDimension, riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[Any] =
    complexByDescriptorFromRepr(from.manifestation, riftDescriptor)
  override def getComplexWithTag[T](from: TDimension, backupRiftDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[T] =
    complexWithTagFromRepr[T](from.manifestation, backupRiftDescriptor)
  
}