package riftwarp.impl.dematerializers

import language.higherKinds
import scala.collection.IterableLike
import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.components._
import riftwarp.ma.HasFunctionObjects

/**
 * Does not implement the up most '...Optional' methods, because they might differ in behaviour.
 */
abstract class BaseDematerializer[TDimension <: RiftDimension](val tDimension: Class[_ <: RiftDimension], hasDecomposers: HasDecomposers) extends Dematerializer[TDimension] {
  type ValueRepr = TDimension#Under
  protected def divertBlob: BlobDivert
  protected def spawnNew(ident: String): Dematerializer[TDimension] = spawnNew(ident :: path)
  /**
   * Creates a new instance of a dematerializer. It should respect divertBlob when creating the new instance.
   *
   */
  protected def spawnNew(path: List[String]): Dematerializer[TDimension]

  protected def valueReprToDim(repr: ValueRepr): TDimension
  protected def dimToReprValue(dim: TDimension): ValueRepr
  protected def addReprValue(ident: String, value: ValueRepr): Dematerializer[TDimension]
  protected def foldReprs(elems: Iterable[ValueRepr]): ValueRepr
  protected def getPrimitiveToRepr[A](implicit tag: ClassTag[A]): AlmValidation[(A => ValueRepr)]
  protected def getAnyPrimitiveToRepr(what: Any): AlmValidation[(Any => ValueRepr)]

  protected def getDematerializedBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier): AlmValidation[Dematerializer[TDimension]] =
    divertBlob(aValue, blobIdentifier).flatMap(blob =>
      blob.decompose(spawnNew(ident)))

  protected def insertDematerializer(ident: String, dematerializer: Dematerializer[TDimension]): Dematerializer[TDimension]

  override def includeDirect[T <: AnyRef](what: T, decomposer: Decomposer[T]): AlmValidation[Dematerializer[TDimension]] = decomposer.decompose(what, this)
  override def include(what: AnyRef, riftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getRawDecomposerFor(what, riftDescriptor).flatMap(decomposer => decomposer.decomposeRaw(what, this))
  override def include[T <: AnyRef](what: T)(implicit tag: ClassTag[T]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getDecomposer[T]().flatMap(decomposer =>
      includeDirect(what, decomposer))

  def getStringRepr(aValue: String): ValueRepr

  def getBooleanRepr(aValue: Boolean): ValueRepr

  def getByteRepr(aValue: Byte): ValueRepr
  def getIntRepr(aValue: Int): ValueRepr
  def getLongRepr(aValue: Long): ValueRepr
  def getBigIntRepr(aValue: BigInt): ValueRepr

  def getFloatRepr(aValue: Float): ValueRepr
  def getDoubleRepr(aValue: Double): ValueRepr
  def getBigDecimalRepr(aValue: BigDecimal): ValueRepr

  def getByteArrayRepr(aValue: Array[Byte]): ValueRepr
  def getBase64EncodedByteArrayRepr(aValue: Array[Byte]): ValueRepr
  def getByteArrayBlobEncodedRepr(aValue: Array[Byte]): ValueRepr

  def getDateTimeRepr(aValue: org.joda.time.DateTime): ValueRepr

  def getUriRepr(aValue: _root_.java.net.URI): ValueRepr

  def getUuidRepr(aValue: _root_.java.util.UUID): ValueRepr


  override def addString(ident: String, aValue: String): Dematerializer[TDimension] = addReprValue(ident, getStringRepr(aValue))

  override def addBoolean(ident: String, aValue: Boolean): Dematerializer[TDimension] = addReprValue(ident, getBooleanRepr(aValue))

  override def addByte(ident: String, aValue: Byte): Dematerializer[TDimension] = addReprValue(ident, getByteRepr(aValue))
  override def addInt(ident: String, aValue: Int): Dematerializer[TDimension] = addReprValue(ident, getIntRepr(aValue))
  override def addLong(ident: String, aValue: Long): Dematerializer[TDimension] = addReprValue(ident, getLongRepr(aValue))
  override def addBigInt(ident: String, aValue: BigInt): Dematerializer[TDimension] = addReprValue(ident, getBigIntRepr(aValue))

  override def addFloat(ident: String, aValue: Float): Dematerializer[TDimension] = addReprValue(ident, getFloatRepr(aValue))
  override def addDouble(ident: String, aValue: Double): Dematerializer[TDimension] = addReprValue(ident, getDoubleRepr(aValue))
  override def addBigDecimal(ident: String, aValue: BigDecimal): Dematerializer[TDimension] = addReprValue(ident, getBigDecimalRepr(aValue))

  override def addByteArray(ident: String, aValue: Array[Byte]): Dematerializer[TDimension] = addReprValue(ident, getByteArrayRepr(aValue))
  override def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): Dematerializer[TDimension] = addReprValue(ident, getBase64EncodedByteArrayRepr(aValue))
  override def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): Dematerializer[TDimension] = addReprValue(ident, getByteArrayBlobEncodedRepr(aValue))

  override def addDateTime(ident: String, aValue: org.joda.time.DateTime): Dematerializer[TDimension] = addReprValue(ident, getDateTimeRepr(aValue))

  override def addUri(ident: String, aValue: _root_.java.net.URI): Dematerializer[TDimension] = addReprValue(ident, getUriRepr(aValue))

  override def addUuid(ident: String, aValue: _root_.java.util.UUID): Dematerializer[TDimension] = addReprValue(ident, getUuidRepr(aValue))

  override def addBlob(ident: String, what: Array[Byte], blobIdentifier: RiftBlobIdentifier): AlmValidation[Dematerializer[TDimension]] = getDematerializedBlob(ident, what, blobIdentifier)
  override def addBlob(ident: String, what: Array[Byte]): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, what, PropertyPath(ident :: path))
  override def addBlob(ident: String, what: Array[Byte], name: String): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, what, PropertyPathAndIdentifier(ident :: path, name))
  override def addBlob(ident: String, what: Array[Byte], identifiers: Map[String, String]): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, what, PropertyPathAndIdentifiers(ident :: path, identifiers))

  override def addWith[A](ident: String, what: A, decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]] =
    decomposes.decompose(what, spawnNew(ident)).map(dematerializedComplex =>
      insertDematerializer(ident, dematerializedComplex))

  override def addComplex[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getDecomposerFor(what, backupRiftDescriptor).flatMap(decomposer => addWith[A](ident, what, decomposer))

  override def addComplexByTag[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getDecomposer[A]().flatMap(decomposer => addWith[A](ident, what, decomposer))

  override def addIterableAllWith[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]] = {
    val mappedV = what.toList.map(x => decomposes.decompose(x, spawnNew(ident + "[?]" :: path)).toAgg)
    mappedV.sequence.map(dematerializedItems =>
      addReprValue(ident, foldReprs(dematerializedItems.map(demat => dimToReprValue(demat.dematerialize)))))
  }

  override def addIterableStrict[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getDecomposerByDescriptorAndThenByTag(riftDesc).flatMap(decomposer => addIterableAllWith(ident, what, decomposer))

  def addIterableOfComplex[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]] = {
    val decomposes = new Decomposes[A] {
      override def decompose[TTDimension <: RiftDimension](elem: A, into: Dematerializer[TTDimension]): AlmValidation[Dematerializer[TTDimension]] =
        hasDecomposers.getDecomposerFor(elem, backupRiftDescriptor).flatMap(_.decompose(elem, into))
    }
    addIterableAllWith(ident, what, decomposes)
  }

  override def addIterableOfPrimitives[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    getPrimitiveToRepr[A](tag).map(primToRepr =>
      addReprValue(ident, foldReprs(what.toList.map(elem => primToRepr(elem)))))

  override def addIterable[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]] = {
    val mappedV = what.toList.map(x => getReprForPrimitiveOrComplex(ident, x, backupRiftDescriptor).toAgg)
    val sequenced = mappedV.sequence
    sequenced.map(elems => addReprValue(ident, foldReprs(elems)))
  }

  override def addMapAllWith[A, B](ident: String, what: scala.collection.Map[A, B], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    getPrimitiveToRepr[A](tag).flatMap { primMapper =>
      val itemsV =
        what.toSeq.map {
          case (a, b) =>
            decomposes.decompose[TDimension](b, spawnNew(ident + s"[${a.toString}]")).map(demat =>
              (primMapper(a), dimToReprValue(demat.dematerialize))).toAgg
        }
      val items = itemsV.toList.sequence.map(_.map { case (reprA, reprB) => foldReprs(reprA :: reprB :: Nil) })
      items.map(tuples => addReprValue(ident, foldReprs(tuples)))
    }

  override def addMapStrict[A, B <: AnyRef](ident: String, what: scala.collection.Map[A, B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]] =
    hasDecomposers.getDecomposerByDescriptorAndThenByTag(riftDesc).flatMap(decomposer =>
      addMapAllWith(ident, what, decomposer))

  override def addMapOfComplex[A, B <: AnyRef](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] = {
    val decomposes = new Decomposes[B] {
      override def decompose[TTDimension <: RiftDimension](elem: B, into: Dematerializer[TTDimension]): AlmValidation[Dematerializer[TTDimension]] =
        hasDecomposers.getDecomposerFor(elem, backupRiftDescriptor).flatMap(_.decompose(elem, into))
    }
    addMapAllWith[A, B](ident, what, decomposes)
  }

  override def addMapOfPrimitives[A, B](ident: String, what: scala.collection.Map[A, B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]] =
    for {
      primMapperA <- getPrimitiveToRepr[A](tagA)
      primMapperB <- getPrimitiveToRepr[B](tagB)
    } yield addReprValue(ident, foldReprs(what.map { case (a, b) => foldReprs(primMapperA(a) :: primMapperB(b) :: Nil) }))

  override def addMap[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    getPrimitiveToRepr[A](tag).flatMap { keyMapper =>
      val itemsV = what.map {
        case (a, b) => getReprForPrimitiveOrComplex(ident, b, backupRiftDescriptor).map(valueRepr =>
          foldReprs(keyMapper(a) :: valueRepr :: Nil)).toAgg
      }
      itemsV.toList.sequence.map(items => addReprValue(ident, foldReprs(items)))
    }

  override def addMapLiberate[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
    getPrimitiveToRepr[A](tag).map { keyMapper =>
      val items = what.map {
        case (a, b) => getReprForPrimitiveOrComplex(ident, b, backupRiftDescriptor).map(valueRepr =>
          foldReprs(keyMapper(a) :: valueRepr :: Nil)).toOption
      }.flatten
      addReprValue(ident, foldReprs(items))
    }

  private def getReprForPrimitiveOrComplex(ident: String, what: Any, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[ValueRepr] =
    getAnyPrimitiveToRepr(what).fold(
      fail =>
        what match {
          case ar: AnyRef => hasDecomposers.getRawDecomposerFor(ar, backupRiftDescriptor).flatMap(decomposer =>
            decomposer.decomposeRaw[TDimension](ar, spawnNew(ident + "[?]"))).map(demat => dimToReprValue(demat.dematerialize))
          case x => UnspecifiedProblem(s"'${x.getClass.getName()}' is not a primitive type nor an AnyRef").failure
        },
      succ => succ(what).success)

}

abstract class ToStringDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseDematerializer[DimensionString](classOf[DimensionCord], hasDecomposers)

abstract class ToCordDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseDematerializer[DimensionCord](classOf[DimensionCord], hasDecomposers)

abstract class ToBinaryDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseDematerializer[DimensionBinary](classOf[DimensionCord], hasDecomposers)

abstract class ToRawMapDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers) extends BaseDematerializer[DimensionRawMap](classOf[DimensionCord], hasDecomposers)
