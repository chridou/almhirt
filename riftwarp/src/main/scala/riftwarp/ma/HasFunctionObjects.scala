package riftwarp.ma

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.RiftDimension
import riftwarp.RiftChannel

trait HasFunctionObjects {
  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]): Unit 
  def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]): Option[MAFunctions[M]] 
  def getMAFunctions[M[_]](implicit mM: Manifest[M[_]]): AlmValidation[MAFunctions[M]] =
    option.cata(tryGetMAFunctions)(
        fo => fo.success, 
        UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.erasure.getName())).failure)

  def addChannelFolder[A, B](fo: RegisterableChannelFolder[A,B]): Unit 
  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A,B]] 
  def getChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Folder[A,B]] =
    option.cata(tryGetChannelFolder[A,B](channel))(
        folder => folder.success, 
        UnspecifiedProblem("No folder found for channel '%s' and A(%s) -> B(%s))".format(channel, mA.erasure.getName(), mB.erasure.getName())).failure)

  def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]): Unit 
  def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: Manifest[M[_]], mN: Manifest[N[_]]): Option[ConvertsMAToNA[M, N]] 
  def getConvertsMAToNA[M[_], N[_]](implicit mM: Manifest[M[_]], mN: Manifest[N[_]]): AlmValidation[ConvertsMAToNA[M, N]] =
    option.cata(tryGetConvertsMAToNA[M,N])(
        folder => folder.success, 
        UnspecifiedProblem("No converter found for M[_](%s[_]) to N[_](%s[_])".format(mM.erasure.getName(), mN.erasure.getName())).failure)
    
}