package riftwarp.impl

import riftwarp._
import riftwarp.ma._

class ConcurrentFunctionObjectRegistry extends HasFunctionObjects {
  import scala.collection.mutable._
  val functionObjects = new _root_.java.util.concurrent.ConcurrentHashMap[String, AnyRef]()
  val channelFolders = new _root_.java.util.concurrent.ConcurrentHashMap[String, AnyRef]()
  val mAToNAConverters = new _root_.java.util.concurrent.ConcurrentHashMap[String, AnyRef]()

  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) {
    if (!functionObjects.contains(fo.tM.getName()))
      functionObjects.put(fo.tM.getName(), fo)
  }

  def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]): Option[MAFunctions[M]] =
    functionObjects.get(mM.erasure.getName()) match {
      case null => None
      case x => Some(x.asInstanceOf[MAFunctions[M]])
    }

  def addChannelFolder[A, B](folder: RegisterableChannelFolder[A, B]) {
    val ident = "%s_%s_%s".format(folder.channel.channelType, folder.tA.getName(), folder.tB.getName())
    if (!channelFolders.contains(ident))
      channelFolders.put(ident, folder)
  }

  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A, B]] = {
    val ident = "%s_%s_%s".format(channel.channelType, mA.erasure.getName(), mB.erasure.getName())
    channelFolders.get(ident) match {
      case null => None
      case x => Some(x.asInstanceOf[RegisterableChannelFolder[A, B]])
    }
  }

  def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]) {
    val ident = "%s_%s".format(converter.tM.getName(), converter.tN.getName())
    if (!mAToNAConverters.contains(ident))
      mAToNAConverters.put(ident, converter)
  }

  def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: Manifest[M[_]], mN: Manifest[N[_]]): Option[ConvertsMAToNA[M, N]] = {
    if (mM == mN)
      Some(new IdentityMAToNAConverter[M].asInstanceOf[ConvertsMAToNA[M, N]])
    else {
      val ident = "%s_%s".format(mM.erasure.getName(), mN.erasure.getName())
      mAToNAConverters.get(ident) match {
        case null => None
        case x => Some(x.asInstanceOf[ConvertsMAToNA[M, N]])
      }
    }
  }
}

object ConcurrentFunctionObjectRegistry {
  def apply() = new ConcurrentFunctionObjectRegistry
}