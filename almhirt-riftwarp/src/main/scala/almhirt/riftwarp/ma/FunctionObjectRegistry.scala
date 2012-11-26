package almhirt.riftwarp.ma

import almhirt.riftwarp.RiftDimension
import almhirt.riftwarp.RiftChannel

trait HasFunctionObjects {
  def addToMADimensionFunctor[M[_]](fo: RegisterableToMADimensionFunctor[M]): Unit 
  def tryGetToMADimensionFunctor[M[_]](implicit mM: Manifest[M[_]]): Option[RegisterableToMADimensionFunctor[M]] 
  def addToMDimensionFold[M[_], TDimension <: RiftDimension](fo: RegisterableToMDimensionFold[M, TDimension]): Unit 
  def tryGetToMDimensionFold[M[_], TDimension <: RiftDimension](channel: RiftChannel)(implicit mM: Manifest[M[_]], mD: Manifest[RiftDimension]): Option[RegisterableToMDimensionFold[M, TDimension]] 
}