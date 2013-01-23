package riftwarp.ma

import language.higherKinds
import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.RiftDimension
import riftwarp.RiftChannel

trait HasFunctionObjects {
  def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]): Unit 
  def tryGetMAFunctions[M[_]](implicit mM: ClassTag[M[_]]): Option[MAFunctions[M]] 
  def getMAFunctions[M[_]](implicit mM: ClassTag[M[_]]): AlmValidation[MAFunctions[M]] =
    option.cata(tryGetMAFunctions[M])(
        fo => fo.success, 
        UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)

  def addChannelFolder[A, B](fo: RegisterableChannelFolder[A,B]): Unit 
  def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: ClassTag[A], mB: ClassTag[B]): Option[Folder[A,B]] 
  def getChannelFolder[A, B](channel: RiftChannel)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Folder[A,B]] =
    option.cata(tryGetChannelFolder[A,B](channel))(
        folder => folder.success, 
        UnspecifiedProblem("No folder found for channel '%s' and A(%s) -> B(%s))".format(channel, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure)

  def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]): Unit 
  def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: ClassTag[M[_]], mN: ClassTag[N[_]]): Option[ConvertsMAToNA[M, N]] 
  def getConvertsMAToNA[M[_], N[_]](implicit mM: ClassTag[M[_]], mN: ClassTag[N[_]]): AlmValidation[ConvertsMAToNA[M, N]] =
    option.cata(tryGetConvertsMAToNA[M,N])(
        folder => folder.success, 
        UnspecifiedProblem("No converter found for M[_](%s[_]) to N[_](%s[_])".format(mM.runtimeClass.getName(), mN.runtimeClass.getName())).failure)
    
}