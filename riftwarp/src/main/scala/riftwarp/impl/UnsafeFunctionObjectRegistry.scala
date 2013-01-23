package riftwarp.impl

import language.higherKinds

import scala.reflect.ClassTag
import riftwarp._
import riftwarp.ma._

class UnsafeFunctionObjectRegistry extends HasFunctionObjects {
  import scala.collection.mutable._
  val functionObjects = HashMap[String, AnyRef]()
  val channelFolders = HashMap[String, AnyRef]()
  val mAToNAConverters = HashMap[String, AnyRef]()

  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) {
    if (!functionObjects.contains(fo.tM.getName()))
      functionObjects += (fo.tM.getName() -> fo)
  }

  def tryGetMAFunctions[M[_]](implicit mM: ClassTag[M[_]]): Option[MAFunctions[M]] =
    functionObjects.get(mM.runtimeClass.getName()).map(_.asInstanceOf[MAFunctions[M]])

  def addChannelFolder[A, B](folder: RegisterableChannelFolder[A, B]) {
    val ident = "%s_%s_%s".format(folder.channel.channelType, folder.tA.getName(), folder.tB.getName())
    if (!channelFolders.contains(ident))
      channelFolders += (ident -> folder)
  }

  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: ClassTag[A], mB: ClassTag[B]): Option[Folder[A, B]] = {
    val ident = "%s_%s_%s".format(channel.channelType, mA.runtimeClass.getName(), mB.runtimeClass.getName())
    channelFolders.get(ident).map(_.asInstanceOf[RegisterableChannelFolder[A, B]])
  }

  def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]) {
    val ident = "%s_%s".format(converter.tM.getName(), converter.tN.getName())
    if (!mAToNAConverters.contains(ident))
      mAToNAConverters += (ident -> converter)
  }

  def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: ClassTag[M[_]], mN: ClassTag[N[_]]): Option[ConvertsMAToNA[M, N]] = {
    if (mM == mN)
      Some(new IdentityMAToNAConverter[M].asInstanceOf[ConvertsMAToNA[M, N]])
    else {
      val ident = "%s_%s".format(mM.runtimeClass.getName(), mN.runtimeClass.getName())
      mAToNAConverters.get(ident).map(_.asInstanceOf[ConvertsMAToNA[M, N]])
    }
  }
}

object UnsafeFunctionObjectRegistry {
  def apply() = new UnsafeFunctionObjectRegistry
}