package riftwarp.impl.dematerializers

import scala.collection.IterableLike
import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.serialization._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.components._
import riftwarp.serialization.common._

/**
 * Does not implement the up most '...Optional' methods, because they might differ in behaviour.
 */
abstract class BaseWarpSequencer[TDimension <: RiftDimension](val tDimension: Class[_ <: RiftDimension], hasDecomposers: HasDecomposers) extends WarpSequencer[TDimension] {
  def dematerializer: Dematerializer[TDimension]
  type ValueRepr = TDimension#Under
  /**
   * Creates a new instance of a warpSequencer. It should respect divertBlob when creating the new instance.
   *
   */
  protected def spawnNew(): WarpSequencer[TDimension]

  protected def addReprValue(ident: String, value: ValueRepr): WarpSequencer[TDimension]

  protected def insertWarpSequencer(ident: String, warpSequencer: WarpSequencer[TDimension]): WarpSequencer[TDimension]

  override def includeDirect[T <: AnyRef](what: T, decomposer: Decomposer[T]): AlmValidation[WarpSequencer[TDimension]] = decomposer.decompose(what, this)
  override def include(what: AnyRef, riftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    hasDecomposers.getRawDecomposerFor(what, riftDescriptor).flatMap(decomposer => decomposer.decomposeRaw(what, this))
  override def include[T <: AnyRef](what: T)(implicit tag: ClassTag[T]): AlmValidation[WarpSequencer[TDimension]] =
    hasDecomposers.getDecomposer[T]().flatMap(decomposer =>
      includeDirect(what, decomposer))

  override def addString(ident: String, aValue: String): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getStringRepr(aValue))

  override def addBoolean(ident: String, aValue: Boolean): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getBooleanRepr(aValue))

  override def addByte(ident: String, aValue: Byte): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getByteRepr(aValue))
  override def addInt(ident: String, aValue: Int): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getIntRepr(aValue))
  override def addLong(ident: String, aValue: Long): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getLongRepr(aValue))
  override def addBigInt(ident: String, aValue: BigInt): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getBigIntRepr(aValue))

  override def addFloat(ident: String, aValue: Float): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getFloatRepr(aValue))
  override def addDouble(ident: String, aValue: Double): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getDoubleRepr(aValue))
  override def addBigDecimal(ident: String, aValue: BigDecimal): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getBigDecimalRepr(aValue))

  override def addByteArray(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getByteArrayRepr(aValue))
  override def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getBase64EncodedByteArrayRepr(aValue))
  override def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getByteArrayBlobEncodedRepr(aValue))

  override def addDateTime(ident: String, aValue: org.joda.time.DateTime): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getDateTimeRepr(aValue))

  override def addUri(ident: String, aValue: _root_.java.net.URI): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getUriRepr(aValue))

  override def addUuid(ident: String, aValue: _root_.java.util.UUID): WarpSequencer[TDimension] = addReprValue(ident, dematerializer.getUuidRepr(aValue))

  override def addWith[A](ident: String, what: A, decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getWithRepr(what, decomposes, spawnNew).map(addReprValue(ident, _))

  override def addComplex[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getComplexRepr(what, backupRiftDescriptor, spawnNew)(hasDecomposers).map(addReprValue(ident, _))

  override def addComplexByTag[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getComplexByTagRepr(what, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))

  override def addIterableAllWith[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getIterableAllWithRepr(what, decomposes, spawnNew).map(addReprValue(ident, _))

  override def addIterableStrict[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getIterableStrictRepr(what, riftDesc, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))

  def addIterableOfComplex[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getIterableOfComplexRepr(what, backupRiftDescriptor, spawnNew)(hasDecomposers).map(addReprValue(ident, _))

  override def addIterableOfPrimitives[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getIterableOfPrimitivesRepr(what).map(addReprValue(ident, _))

  override def addIterable[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getIterableRepr(what, backupRiftDescriptor, spawnNew)(hasDecomposers).map(addReprValue(ident, _))

  override def addMapAllWith[A, B](ident: String, what: scala.collection.Map[A, B], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapAllWithRepr(what, decomposes, spawnNew).map(addReprValue(ident, _))

  override def addMapStrict[A, B <: AnyRef](ident: String, what: scala.collection.Map[A, B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapStrictRepr(what, riftDesc, spawnNew)(implicitly[ClassTag[A]], implicitly[ClassTag[B]], hasDecomposers).map(addReprValue(ident, _))

  override def addMapOfComplex[A, B <: AnyRef](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapOfComplexRepr(what, backupRiftDescriptor, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))

  override def addMapOfPrimitives[A, B](ident: String, what: scala.collection.Map[A, B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapOfPrimitivesRepr(what).map(addReprValue(ident, _))

  override def addMap[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapRepr(what, backupRiftDescriptor, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))

  override def addMapLiberate[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getMapLiberateRepr(what, backupRiftDescriptor, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))

  override def addTreeAllWith[A](ident: String, what: scalaz.Tree[A], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getTreeAllWithRepr[A](what, decomposes, spawnNew).map(addReprValue(ident, _))
  override def addTreeStrict[A <: AnyRef](ident: String, what: scalaz.Tree[A], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getTreeStrictRepr[A](what, riftDesc, spawnNew)(implicitly[ClassTag[A]], hasDecomposers).map(addReprValue(ident, _))
  override def addTreeOfComplex[A <: AnyRef](ident: String, what: scalaz.Tree[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getTreeOfComplexRepr[A](what, backupRiftDescriptor, spawnNew)(hasDecomposers).map(addReprValue(ident, _))
  override def addTreeOfPrimitives[A](ident: String, what: scalaz.Tree[A])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getTreeOfPrimitivesRepr[A](what)(implicitly[ClassTag[A]]).map(addReprValue(ident, _))
  override def addTree[A](ident: String, what: scalaz.Tree[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] =
    dematerializer.getTreeRepr[A](what, backupRiftDescriptor, spawnNew)(hasDecomposers).map(addReprValue(ident, _))

}

abstract class ToStringWarpSequencer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseWarpSequencer[DimensionString](classOf[DimensionCord], hasDecomposers)

abstract class ToCordWarpSequencer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseWarpSequencer[DimensionCord](classOf[DimensionCord], hasDecomposers)

abstract class ToBinaryWarpSequencer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseWarpSequencer[DimensionBinary](classOf[DimensionBinary], hasDecomposers)

abstract class ToRawMapWarpSequencer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseWarpSequencer[DimensionRawMap](classOf[DimensionRawMap], hasDecomposers)
