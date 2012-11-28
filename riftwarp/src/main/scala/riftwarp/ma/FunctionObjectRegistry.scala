package riftwarp.ma

import riftwarp.RiftDimension
import riftwarp.RiftChannel

trait HasFunctionObjects {
  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]): Unit 
  def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]): Option[MAFunctions[M]] 
  def addChannelFolder[A, B](fo: RegisterableChannelFolder[A,B]): Unit 
  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A,B]] 
}