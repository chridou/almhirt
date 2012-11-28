package riftwarp.ma

import almhirt.common._
import riftwarp._

trait Unfolder[A, B] {
  def unfold[M[_]](s: A)(funcObj: MAFunctions[M]): AlmValidation[M[B]] 
}

trait RegisterableChannelUnfolder[A, B] extends Unfolder[A, B] {
  def channel: RiftChannel
  def tA: Class[_ <: A]
  def tB: Class[_ <: B]
}