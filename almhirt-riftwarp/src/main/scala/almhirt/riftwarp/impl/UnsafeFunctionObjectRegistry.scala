package almhirt.riftwarp.impl

import almhirt.riftwarp._
import almhirt.riftwarp.ma._

class UnsafeFunctionObjectRegistry extends HasFunctionObjects {
  import scala.collection.mutable._
  val toDimensionFunctors = HashMap[String, AnyRef]()
  val toDimensionFolders = HashMap[String, AnyRef]()

  def addToMADimensionFunctor[M[_]](fo: RegisterableToMADimensionFunctor[M]){
    if(!toDimensionFunctors.contains(fo.tM.getName()))
      toDimensionFunctors += (fo.tM.getName() -> fo)
  } 
  
  def tryGetToMADimensionFunctor[M[_]](implicit mM: Manifest[M[_]]): Option[RegisterableToMADimensionFunctor[M]] = {
    toDimensionFunctors.get(mM.erasure.getName()).map(_.asInstanceOf[RegisterableToMADimensionFunctor[M]])
  }
  
  def addToMDimensionFold[M[_], TDimension <: RiftDimension](fo: RegisterableToMDimensionFold[M, TDimension]){
    val ident = "%s_%s_%s".format(fo.channel.channelType, fo.tDim.getName(), fo.tM.getName())
     if(!toDimensionFolders.contains(ident))
      toDimensionFolders += (ident -> fo)
 }
  
  def tryGetToMDimensionFold[M[_], TDimension <: RiftDimension](channel: RiftChannel)(implicit mM: Manifest[M[_]], mD: Manifest[RiftDimension]): Option[RegisterableToMDimensionFold[M, TDimension]] = {
    val ident = "%s_%s_%s".format(channel.channelType, mD.erasure.getName(), mM.erasure.getName())
    toDimensionFolders.get(ident).map(_.asInstanceOf[RegisterableToMDimensionFold[M, TDimension]])
  }
}

object UnsafeFunctionObjectRegistry {
  def apply() = new UnsafeFunctionObjectRegistry
}