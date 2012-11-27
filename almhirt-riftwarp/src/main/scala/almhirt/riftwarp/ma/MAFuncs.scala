package almhirt.riftwarp.ma

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

object MAFuncs {
  def map[M[_], A, B](ma: M[A])(map: A => B)(implicit functions: HasFunctionObjects, mM: Manifest[M[_]]): AlmValidation[M[B]] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo => {
        fo.map(ma)(map).success
      },
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.erasure.getName())).failure)
  }

  def fold[M[_], A, B](channel: RiftChannel)(ma: M[A])(implicit functions: HasFunctionObjects, mM: Manifest[M[_]], mA: Manifest[A], mB: Manifest[B]): AlmValidation[B] = {
    option.cata(functions.tryGetMAFunctions[M])(
      fo => {
        option.cata(functions.tryGetChannelFolder[A, B](channel))(
          folder => {
            folder.fold(ma)(fo)
          },
          UnspecifiedProblem("No function folder found for A(%s) and B(%s)".format(mA.erasure.getName(), mA.erasure.getName())).failure)
      },
      UnspecifiedProblem("No function object found for M[_](%s[_])".format(mM.erasure.getName())).failure)
  }
}