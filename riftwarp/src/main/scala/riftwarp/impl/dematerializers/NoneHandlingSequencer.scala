package riftwarp.impl.dematerializers

import language.higherKinds
import scala.collection.IterableLike
import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

trait NoneIsHandledUnified[TDimension <: RiftDimension] { warpSequencer: WarpSequencer[TDimension] =>
  protected def noneHandler(ident: String): WarpSequencer[TDimension]

  override def addOptionalString(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addString(ident, _), noneHandler(ident))

  override def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = option.cata(anOptionalValue)(addBoolean(ident, _), noneHandler(ident))

  override def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = option.cata(anOptionalValue)(addByte(ident, _), noneHandler(ident))
  override def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = option.cata(anOptionalValue)(addInt(ident, _), noneHandler(ident))
  override def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = option.cata(anOptionalValue)(addLong(ident, _), noneHandler(ident))
  override def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = option.cata(anOptionalValue)(addBigInt(ident, _), noneHandler(ident))

  override def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = option.cata(anOptionalValue)(addFloat(ident, _), noneHandler(ident))
  override def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = option.cata(anOptionalValue)(addDouble(ident, _), noneHandler(ident))
  override def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = option.cata(anOptionalValue)(addBigDecimal(ident, _), noneHandler(ident))

  override def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArray(ident, _), noneHandler(ident))
  override def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addBase64EncodedByteArray(ident, _), noneHandler(ident))
  override def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArrayBlobEncoded(ident, _), noneHandler(ident))

  override def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = option.cata(anOptionalValue)(addDateTime(ident, _), noneHandler(ident))

  override def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]) = option.cata(anOptionalValue)(addUri(ident, _), noneHandler(ident))

  override def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = option.cata(anOptionalValue)(addUuid(ident, _), noneHandler(ident))

  override def addOptionalBlob(ident: String, what: Option[Array[Byte]], blobIdentifier: RiftBlobIdentifier) = option.cata(what)(addBlob(ident, _, blobIdentifier), noneHandler(ident).success)
  override def addOptionalBlob(ident: String, what: Option[Array[Byte]]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addBlob(ident, _), noneHandler(ident).success)
  override def addOptionalBlob(ident: String, what: Option[Array[Byte]], name: String): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addBlob(ident, _, name), noneHandler(ident).success)
  override def addOptionalBlob(ident: String, what: Option[Array[Byte]], identifiers: Map[String, String]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addBlob(ident, _, identifiers), noneHandler(ident).success)

  override def addOptionalWith[A](ident: String, what: Option[A], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addWith(ident, _, decomposes), noneHandler(ident).success)
  override def addOptionalComplex[A <: AnyRef](ident: String, what: Option[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addComplex(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  override def addOptionalComplexByTag[A <: AnyRef](ident: String, what: Option[A])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addComplexByTag(ident, _), noneHandler(ident).success)

  override def addOptionalIterableAllWith[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addIterableAllWith(ident, _, decomposes), noneHandler(ident).success)
  override def addOptionalIterableStrict[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addIterableStrict(ident, _, riftDesc)(tag), noneHandler(ident).success)
  override def addOptionalIterableOfComplex[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addIterableOfComplex(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  override def addOptionalIterableOfPrimitives[A, Coll](ident: String, what: Option[IterableLike[A, Coll]])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addIterableOfPrimitives(ident, _)(tag), noneHandler(ident).success)
  override def addOptionalIterable[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addIterable(ident, _, backupRiftDescriptor), noneHandler(ident).success)

  override def addOptionalMapAllWith[A, B](ident: String, what: Option[scala.collection.Map[A, B]], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMapAllWith(ident, _, decomposes), noneHandler(ident).success)
  override def addOptionalMapStrict[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A, B]], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMapStrict(ident, _, riftDesc), noneHandler(ident).success)
  override def addOptionalMapOfComplex[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A, B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMapOfComplex(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  override def addOptionalMapOfPrimitives[A, B](ident: String, what: Option[scala.collection.Map[A, B]])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMapOfPrimitives(ident, _), noneHandler(ident).success)
  override def addOptionalMap[A, B](ident: String, what: Option[scala.collection.Map[A, B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMap(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  override def addOptionalMapLiberate[A, B](ident: String, what: Option[scala.collection.Map[A, B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = option.cata(what)(addMapLiberate(ident, _, backupRiftDescriptor), noneHandler(ident).success)

  override def addOptionalTreeAllWith[A](ident: String, what: Option[scalaz.Tree[A]], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]] = 
    option.cata(what)(addTreeAllWith(ident, _, decomposes), noneHandler(ident).success)
  override def addOptionalTreeStrict[A <: AnyRef](ident: String, what: Option[scalaz.Tree[A]], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = 
    option.cata(what)(addTreeStrict(ident, _, riftDesc), noneHandler(ident).success)
  override def addOptionalTreeOfComplex[A <: AnyRef](ident: String, what: Option[scalaz.Tree[A]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] = 
    option.cata(what)(addTreeOfComplex(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  override def addOptionalTreeOfPrimitives[A](ident: String, what: Option[scalaz.Tree[A]])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]] = 
    option.cata(what)(addTreeOfPrimitives(ident, _), noneHandler(ident).success)
  override def addOptionalTree[A](ident: String, what: Option[scalaz.Tree[A]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]] = 
    option.cata(what)(addTree(ident, _, backupRiftDescriptor), noneHandler(ident).success)
  
}

trait NoneIsOmmitted[TDimension <: RiftDimension] { warpSequencer: WarpSequencer[TDimension] with NoneIsHandledUnified[TDimension] =>
  protected override def noneHandler(ident: String): WarpSequencer[TDimension] = warpSequencer
}
