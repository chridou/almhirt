package riftwarp

import almhirt.common._
import riftwarp.components._

trait RawDecomposer extends HasAlternativeRiftDescriptors {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]]
}

trait Decomposes[-T] extends {
  def decompose[TDimension <: RiftDimension](what: T, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]]
}

trait DecomposesAsync[-T] {
  def decomposeAsync[TDimension <: RiftDimension](what: T, into: WarpSequencer[TDimension])(implicit hasExecContext: HasExecutionContext): AlmFuture[WarpSequencer[TDimension]]
}

/**
 * instance -> Atoms
 */
trait Decomposer[-T <: AnyRef] extends RawDecomposer with Decomposes[T] with DecomposesAsync[T] {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    almhirt.almvalidation.funs.computeSafely(decompose[TDimension](what.asInstanceOf[T], into))
  def decompose[TDimension <: RiftDimension](what: T, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]]
  override def decomposeAsync[TDimension <: RiftDimension](what: T, into: WarpSequencer[TDimension])(implicit hasExecContext: HasExecutionContext): AlmFuture[WarpSequencer[TDimension]] =
    AlmFuture { decompose[TDimension](what, into) }
}