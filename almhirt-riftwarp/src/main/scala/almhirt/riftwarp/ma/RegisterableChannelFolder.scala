package almhirt.riftwarp.ma

import almhirt.common._
import almhirt.riftwarp._

trait ChannelFolder[A, B] {
  def fold[M[_]](ma: M[A])(funcObj: MAFunctions[M]): AlmValidation[B]
}

trait RegisterableChannelFolder[A, B] extends ChannelFolder[A, B] {
  def channel: RiftChannel
  def tA: Class[_ <: A]
  def tB: Class[_ <: B]
}