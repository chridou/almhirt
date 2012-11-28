package riftwarp.ma

import almhirt.common._
import riftwarp._

trait Folder[A, B] {
  def fold[M[_]](ma: M[A])(funcObj: MAFunctions[M]): AlmValidation[B]
}

trait RegisterableChannelFolder[A, B] extends Folder[A, B] {
  def channel: RiftChannel
  def tA: Class[_ <: A]
  def tB: Class[_ <: B]
}