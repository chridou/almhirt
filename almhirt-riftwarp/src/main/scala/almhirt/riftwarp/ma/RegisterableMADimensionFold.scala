package almhirt.riftwarp.ma

import almhirt.riftwarp._

trait RegisterableToMDimensionFold[M[_], TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tM: Class[M[_]]
  def tDim: Class[_ <: RiftDimension]
  def fold(mDim: M[TDimension]): TDimension
}