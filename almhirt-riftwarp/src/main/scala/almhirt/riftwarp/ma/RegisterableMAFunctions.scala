package almhirt.riftwarp.ma

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._

trait MAFunctions[M[_]] {
  def map[A, B](ma: M[A])(f: A => B): M[B]
  def fold[A,B](ma: M[A])(acc: B)(f: (B, A) => B): B
  def uncurriedFold[A,B](ma: M[A], acc: B, f: (B, A) => B): B = fold(ma)(acc)(f)
  def sequenceValidations[A](ma: M[AlmValidationAP[A]]): AlmValidationAP[M[A]]
  def isEmpty(ma: M[_]): Boolean
  def headOption[A](ma: M[A]): Option[A]
  def head[A](ma: M[A]): A
  def tail[A](ma: M[A]): M[A]
  def hasLinearCharacteristics: Boolean
}

trait RegisterableMAFunctions[M[_]] extends MAFunctions[M]{
  def tM: Class[M[_]]
}

trait ListFunctionObject extends RegisterableMAFunctions[List] {
  val tM = classOf[List[_]]
  def map[A, B](ma: List[A])(f: A => B): List[B] = ma.map(f)
  def fold[A,B](ma: List[A])(acc: B)(f: (B, A) => B): B = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: List[AlmValidationAP[A]]): AlmValidationAP[List[A]] = ma.sequence
  def isEmpty(ma: List[_]) = ma.isEmpty
  def headOption[A](ma: List[A]) = ma.headOption
  def head[A](ma: List[A]) = ma.head
  def tail[A](ma: List[A]): List[A] = ma.tail
  val hasLinearCharacteristics = true
}

trait VectorFunctionObject extends RegisterableMAFunctions[Vector] {
  val tM = classOf[Vector[_]]
  def map[A, B](ma: Vector[A])(f: A => B): Vector[B] = ma.map(f)
  def fold[A,B](ma: Vector[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Vector[AlmValidationAP[A]]): AlmValidationAP[Vector[A]] = ma.toList.sequence.map(x => Vector(x: _*))
  def isEmpty(ma: Vector[_]) = ma.isEmpty
  def headOption[A](ma: Vector[A]) = ma.headOption
  def head[A](ma: Vector[A]) = ma.head
  def tail[A](ma: Vector[A]): Vector[A] = ma.tail
  val hasLinearCharacteristics = true
}

trait SetFunctionObject extends RegisterableMAFunctions[Set] {
  val tM = classOf[Set[_]]
  def map[A, B](ma: Set[A])(f: A => B): Set[B] = ma.map(f)
  def fold[A,B](ma: Set[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Set[AlmValidationAP[A]]): AlmValidationAP[Set[A]] = ma.toList.sequence.map(x => Set(x: _*))
  def isEmpty(ma: Set[_]) = ma.isEmpty
  def headOption[A](ma: Set[A]) = ma.headOption
  def head[A](ma: Set[A]) = ma.head
  def tail[A](ma: Set[A]): Set[A] = ma.tail
  val hasLinearCharacteristics = true
}

trait IterableFunctionObject extends RegisterableMAFunctions[Iterable] {
  val tM = classOf[Iterable[_]]
  def map[A, B](ma: Iterable[A])(f: A => B): Iterable[B] = ma.map(f)
  def fold[A,B](ma: Iterable[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Iterable[AlmValidationAP[A]]): AlmValidationAP[Iterable[A]] = ma.toList.sequence.map(x => Iterable(x: _*))
  def isEmpty(ma: Iterable[_]) = ma.isEmpty
  def headOption[A](ma: Iterable[A]) = ma.headOption
  def head[A](ma: Iterable[A]) = ma.head
  def tail[A](ma: Iterable[A]): Iterable[A] = ma.tail
  val hasLinearCharacteristics = true
}

trait TreeFunctionObject extends RegisterableMAFunctions[scalaz.Tree] {
  val tM = classOf[scalaz.Tree[_]]
  def map[A, B](ma: scalaz.Tree[A])(f: A => B): scalaz.Tree[B] = ma.map(f)
  def fold[A,B](ma: scalaz.Tree[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: scalaz.Tree[AlmValidationAP[A]]): AlmValidationAP[scalaz.Tree[A]] = sys.error("not implemented")
  def isEmpty(ma: scalaz.Tree[_]) = false
  def headOption[A](ma: scalaz.Tree[A]) = sys.error("not implemented")
  def head[A](ma: scalaz.Tree[A]) = sys.error("not implemented")
  def tail[A](ma: scalaz.Tree[A]): scalaz.Tree[A] = sys.error("not implemented")
  val hasLinearCharacteristics = false
}

object RegisterableToMADimensionFunctors {
  object listFunctionObject extends ListFunctionObject
  object vectorFunctionObject extends VectorFunctionObject
  object setFunctionObject extends SetFunctionObject
  object iterableFunctionObject extends IterableFunctionObject
  object treeFunctionObject extends TreeFunctionObject
}


