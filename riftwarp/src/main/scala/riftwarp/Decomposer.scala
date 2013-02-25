package riftwarp

import almhirt.common._
import riftwarp.components._

trait RawDecomposer extends HasAlternativeRiftDescriptors {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}

trait Decomposes[-T]{
  def decompose[TDimension <: RiftDimension](what: T, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
}

trait DecomposesAsync[-T]{
  def decomposeAsync[TDimension <: RiftDimension](what: T, into: Dematerializer[TDimension])(implicit hasExecContext: HasExecutionContext): AlmFuture[Dematerializer[TDimension]]
}

/**
 * instance -> Atoms
 */
trait Decomposer[-T <: AnyRef] extends RawDecomposer with Decomposes[T] with DecomposesAsync[T] {
  def decomposeRaw[TDimension <: RiftDimension](what: AnyRef, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    almhirt.almvalidation.funs.computeSafely(decompose[TDimension](what.asInstanceOf[T], into))
  def decompose[TDimension <: RiftDimension](what: T, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]]
  override def decomposeAsync[TDimension <: RiftDimension](what: T, into: Dematerializer[TDimension])(implicit hasExecContext: HasExecutionContext): AlmFuture[Dematerializer[TDimension]] =
    AlmFuture { decompose[TDimension](what, into) }
}