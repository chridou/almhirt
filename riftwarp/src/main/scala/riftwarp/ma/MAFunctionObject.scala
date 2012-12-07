package riftwarp.ma

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait MAFunctions[M[_]] {
  def map[A, B](ma: M[A])(f: A => B): M[B]
  def fold[A, B](ma: M[A])(acc: B)(f: (B, A) => B): B
  def uncurriedFold[A, B](ma: M[A], acc: B, f: (B, A) => B): B = fold(ma)(acc)(f)
  def sequenceValidations[A](ma: M[AlmValidationAP[A]]): AlmValidationAP[M[A]]
  def isEmpty(ma: M[_]): Boolean
  def hasLinearCharacteristics: Boolean
}

trait LinearMAFunctions[M[_]] extends MAFunctions[M] { self: MAFunctions[M] =>
  def mapi[A, B](ma: M[A])(f: (A, Int) => B): M[B]
  def headOption[A](ma: M[A]): Option[A]
  def head[A](ma: M[A]): A
  def tail[A](ma: M[A]): M[A]
  def toList[A](ma: M[A]): List[A]
  val hasLinearCharacteristics = true
}

trait NonLinearMAFunctions[M[_]] extends MAFunctions[M] { self: MAFunctions[M] =>
  def maps[A, B](ma: M[A])(f: (A, String) => B): M[B]
  val hasLinearCharacteristics = false
}

trait RegisterableMAFunctions[M[_]] extends MAFunctions[M] {
  def tM: Class[M[_]]
}

trait ListFunctionObject extends RegisterableMAFunctions[List] with LinearMAFunctions[List] {
  val tM = classOf[List[_]]
  def map[A, B](ma: List[A])(f: A => B): List[B] = ma.map(f)
  def mapi[A, B](ma: List[A])(f: (A, Int) => B): List[B] = {
    var i = -1
    ma.map(x => { i = i + 1; f(x, i) })
  }
  def fold[A, B](ma: List[A])(acc: B)(f: (B, A) => B): B = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: List[AlmValidationAP[A]]): AlmValidationAP[List[A]] = ma.sequence
  def isEmpty(ma: List[_]) = ma.isEmpty
  def headOption[A](ma: List[A]) = ma.headOption
  def head[A](ma: List[A]) = ma.head
  def tail[A](ma: List[A]): List[A] = ma.tail
  def toList[A](ma: List[A]) = ma
}

trait VectorFunctionObject extends RegisterableMAFunctions[Vector] with LinearMAFunctions[Vector] {
  val tM = classOf[Vector[_]]
  def map[A, B](ma: Vector[A])(f: A => B): Vector[B] = ma.map(f)
  def mapi[A, B](ma: Vector[A])(f: (A, Int) => B): Vector[B] = {
    var i = -1
    ma.map(x => { i = i + 1; f(x, i) })
  }
  def fold[A, B](ma: Vector[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Vector[AlmValidationAP[A]]): AlmValidationAP[Vector[A]] = ma.toList.sequence.map(x => Vector(x: _*))
  def isEmpty(ma: Vector[_]) = ma.isEmpty
  def headOption[A](ma: Vector[A]) = ma.headOption
  def head[A](ma: Vector[A]) = ma.head
  def tail[A](ma: Vector[A]): Vector[A] = ma.tail
  def toList[A](ma: Vector[A]) = ma.toList
}

trait SetFunctionObject extends RegisterableMAFunctions[Set] with LinearMAFunctions[Set] {
  val tM = classOf[Set[_]]
  def map[A, B](ma: Set[A])(f: A => B): Set[B] = ma.map(f)
  def mapi[A, B](ma: Set[A])(f: (A, Int) => B): Set[B] = {
    var i = -1
    ma.map(x => { i = i + 1; f(x, i) })
  }
  def fold[A, B](ma: Set[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Set[AlmValidationAP[A]]): AlmValidationAP[Set[A]] = ma.toList.sequence.map(x => Set(x: _*))
  def isEmpty(ma: Set[_]) = ma.isEmpty
  def headOption[A](ma: Set[A]) = ma.headOption
  def head[A](ma: Set[A]) = ma.head
  def tail[A](ma: Set[A]): Set[A] = ma.tail
  def toList[A](ma: Set[A]) = ma.toList
}

trait IterableFunctionObject extends RegisterableMAFunctions[Iterable] with LinearMAFunctions[Iterable] {
  val tM = classOf[Iterable[_]]
  def map[A, B](ma: Iterable[A])(f: A => B): Iterable[B] = ma.map(f)
  def mapi[A, B](ma: Iterable[A])(f: (A, Int) => B): Iterable[B] = {
    var i = -1
    ma.map(x => { i = i + 1; f(x, i) })
  }
  def fold[A, B](ma: Iterable[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: Iterable[AlmValidationAP[A]]): AlmValidationAP[Iterable[A]] = ma.toList.sequence.map(x => Iterable(x: _*))
  def isEmpty(ma: Iterable[_]) = ma.isEmpty
  def headOption[A](ma: Iterable[A]) = ma.headOption
  def head[A](ma: Iterable[A]) = ma.head
  def tail[A](ma: Iterable[A]): Iterable[A] = ma.tail
  def toList[A](ma: Iterable[A]) = ma.toList
}

trait TreeFunctionObject extends RegisterableMAFunctions[scalaz.Tree] with NonLinearMAFunctions[scalaz.Tree] {
  val tM = classOf[scalaz.Tree[_]]
  def map[A, B](ma: scalaz.Tree[A])(f: A => B): scalaz.Tree[B] = ma.map(f)
  def fold[A, B](ma: scalaz.Tree[A])(acc: B)(f: (B, A) => B) = ma.foldLeft(acc)(f)
  def sequenceValidations[A](ma: scalaz.Tree[AlmValidationAP[A]]): AlmValidationAP[scalaz.Tree[A]] = sys.error("'TreeFunctionObject:sequenceValidations' not implemented")
  def isEmpty(ma: scalaz.Tree[_]) = false
  def maps[A, B](ma: scalaz.Tree[A])(f: (A, String) => B): scalaz.Tree[B] = {
    var i = -1
    def getLabel(a: A): String = {
      i = i + 1
      i.toString
    }
    ma.map(x => f(x, getLabel(x)))
  }
}

object RegisterableFunctionObjects {
  object listFunctionObject extends ListFunctionObject
  object vectorFunctionObject extends VectorFunctionObject
  object setFunctionObject extends SetFunctionObject
  object iterableFunctionObject extends IterableFunctionObject
  object treeFunctionObject extends TreeFunctionObject
}


