package riftwarp.ma

import language.higherKinds

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object MAFuncs {
  def map[M[_], A, B](ma: M[A])(map: (A) => B)(implicit functions: HasFunctionObjects, mM: Manifest[M[_]]): AlmValidation[M[B]] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo => fo.map(ma)(map).success,
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)
  }

  def mapV[M[_], A, B](ma: M[A])(map: (A) => AlmValidation[B])(implicit functions: HasFunctionObjects, mM: Manifest[M[_]]): AlmValidation[M[B]] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo => fo.sequenceValidations(fo.map(ma)(a => map(a).toAgg)),
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)
  }
  
  
  def mapi[M[_], A, B](ma: M[A])(map: (A, String) => B)(implicit functions: HasFunctionObjects, mM: Manifest[M[_]]): AlmValidation[M[B]] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo =>
        (fo: @unchecked) match {
          case fo: LinearMAFunctions[M] =>
            fo.mapi(ma)((a, i) => map(a, "["+i.toString+"]")).success
          case fo: NonLinearMAFunctions[M] =>
            fo.maps(ma)((a, s) => map(a, "["+s+"]")).success
          case x =>
            UnspecifiedProblem("Not yet supported: %s".format(x)).failure
        },
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)
  }
//      fo => fo.sequenceValidations(fo.map(fo.map(ma)(map))(x => x.toAgg)),

  def mapiV[M[_], A, B](ma: M[A])(map: (A, String) => AlmValidation[B])(implicit functions: HasFunctionObjects, mM: Manifest[M[_]]): AlmValidation[M[B]] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo =>
        (fo: @unchecked) match {
          case fo: LinearMAFunctions[M] =>
            fo.sequenceValidations(fo.mapi(ma)((a, i) => map(a, "["+i.toString+"]").toAgg))
          case fo: NonLinearMAFunctions[M] =>
            fo.sequenceValidations(fo.maps(ma)((a, s) => map(a, "["+s+"]").toAgg))
          case x =>
            UnspecifiedProblem("Not yet supported: %s".format(x)).failure
        },
        
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)
  }

  def fold[M[_], A, B](channel: RiftChannel)(ma: M[A])(implicit functions: HasFunctionObjects, mM: Manifest[M[_]], mA: Manifest[A], mB: Manifest[B]): AlmValidation[B] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo => option.cata(functions.tryGetChannelFolder[A, B](channel))(
        folder => folder.fold(ma)(fo),
        UnspecifiedProblem("No function folder found for A(%s) and B(%s) on channel '%s'".format(mA.runtimeClass.getName(), mB.runtimeClass.getName(), channel.channelType)).failure),
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.runtimeClass.getName())).failure)
  }
}