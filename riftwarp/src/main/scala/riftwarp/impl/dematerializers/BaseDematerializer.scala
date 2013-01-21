package riftwarp.impl.dematerializers

import language.higherKinds

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._
import riftwarp.ma.HasFunctionObjects

/**
 * Does not implement the up most '...Optional' methods, because they might differ in behaviour.
 */
abstract class BaseDematerializer[TDimension <: RiftDimension](val tDimension: Class[_ <: RiftDimension], hasDecomosers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends Dematerializer[TDimension] {
  protected def divertBlob: BlobDivert
  protected def spawnNew(ident: String): AlmValidation[Dematerializer[TDimension]] = spawnNew(ident :: path)
  /**
   * Creates a new instance of a dematerializer. It should respect divertBlob when creating the new instance.
   *
   */
  protected def spawnNew(path: List[String]): AlmValidation[Dematerializer[TDimension]]
  protected def getDematerializedBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier): AlmValidation[Dematerializer[TDimension]] =
    spawnNew(ident).flatMap(demat =>
      divertBlob(aValue, blobIdentifier).flatMap(blob =>
        blob.decompose(demat)))

  protected def insertDematerializer(ident: String, dematerializer: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]      
        
  override def addComplexSelective(ident: String, decomposer: RawDecomposer, complex: AnyRef): AlmValidation[Dematerializer[TDimension]] =
    for {
      demat <- spawnNew(ident)
      dematerializedComplex <- decomposer.decomposeRaw(complex)(demat)
      result <- insertDematerializer(ident, dematerializedComplex)
    } yield result

  override def addComplexFixed(ident: String, complex: AnyRef, descriptor: RiftDescriptor): AlmValidation[Dematerializer[TDimension]] =
    hasDecomosers.getRawDecomposer(descriptor).flatMap(decomposer =>
      addComplexSelective(ident, decomposer, complex))
}

abstract class ToStringDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomosers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionString](classOf[DimensionCord], hasDecomosers, hasFunctionObjects)

abstract class ToCordDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomosers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionCord](classOf[DimensionCord], hasDecomosers, hasFunctionObjects)

abstract class ToBinaryDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomosers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionBinary](classOf[DimensionCord], hasDecomosers, hasFunctionObjects)

abstract class ToRawMapDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup, hasDecomosers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionRawMap](classOf[DimensionCord], hasDecomosers, hasFunctionObjects)
