package riftwarp.ma

import almhirt.common._

trait ConvertsMAToNA[M[_], N[_]] {
   def convert[A](ma: M[A]): AlmValidation[N[A]]
}

trait RegisterableConvertsMAToNA[M[_], N[_]] extends ConvertsMAToNA[M, N] {
  def tM: Class[_ <: M[_]]
  def tN: Class[_ <: N[_]]
}

