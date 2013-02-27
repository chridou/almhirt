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
abstract class BaseDematerializer[TDimension <: RiftDimension](val tDimension: Class[_ <: RiftDimension], hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends Dematerializer[TDimension] {
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
    val mappedV = what.toList.map(x =>
      getAnyPrimitiveToRepr(x).fold(
        fail =>
          x match {
            case ar: AnyRef => hasDecomposers.getRawDecomposerFor(ar, backupRiftDescriptor).flatMap(decomposer =>
              decomposer.decomposeRaw[TDimension](ar, spawnNew(ident + "[?]"))).map(demat => dimToReprValue(demat.dematerialize))
            case x => UnspecifiedProblem(s"'${x.getClass.getName()}' is not a primitive type nor an AnyRef").failure
          },
        succ => succ(x).success).toAgg)
    val sequenced = mappedV.sequence
    sequenced.map(elems => addReprValue(ident, foldReprs(elems)))
  }
  
  def addMapAllWith[A, B](ident: String, what: scala.collection.Map[A,B], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]] =
//    getPrimitiveToRepr[A](tag).flatMap(primMapper =>
//      what.toSeq.map{ case (a, b) => 
//        val itemV = decomposes.decompose[TDimension](b, spawnNew(ident + s"[${a.toString}]")).map(demat =>
//          primMapper(a, dimToReprValue(demat.dematerialize)))
//          ???})
    ???
  
}

abstract class ToStringDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionString](classOf[DimensionCord], hasDecomposers, hasFunctionObjects)

abstract class ToCordDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionCord](classOf[DimensionCord], hasDecomposers, hasFunctionObjects)

abstract class ToBinaryDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionBinary](classOf[DimensionCord], hasDecomposers, hasFunctionObjects)

abstract class ToRawMapDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionRawMap](classOf[DimensionCord], hasDecomposers, hasFunctionObjects)
