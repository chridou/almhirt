package riftwarp.impl

import riftwarp._
import riftwarp.ma._

class UnsafeFunctionObjectRegistry extends HasFunctionObjects {
  import scala.collection.mutable._
  val functionObjects = HashMap[String, AnyRef]()
  val channelFolders = HashMap[String, AnyRef]()

  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) {
    if(!functionObjects.contains(fo.tM.getName()))
      functionObjects += (fo.tM.getName() -> fo)
  } 
  
  def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]): Option[MAFunctions[M]] =
    functionObjects.get(mM.erasure.getName()).map(_.asInstanceOf[MAFunctions[M]])
  
  def addChannelFolder[A, B](folder: RegisterableChannelFolder[A,B]) { 
    val ident = "%s_%s_%s".format(folder.channel.channelType, folder.tA.getName(), folder.tB.getName())
     if(!channelFolders.contains(ident))
      channelFolders += (ident -> folder)
 }
  
  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A,B]] = { 
    val ident = "%s_%s_%s".format(channel.channelType, mA.erasure.getName(), mB.erasure.getName())
    channelFolders.get(ident).map(_.asInstanceOf[RegisterableChannelFolder[A, B]])
  }
}

object UnsafeFunctionObjectRegistry {
  def apply() = new UnsafeFunctionObjectRegistry
}