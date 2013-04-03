package almhirt.almvalidation

import scalaz.syntax.Ops
import scalaz.syntax.validation._
import almhirt.common._

trait AlmBooleanConstraints extends Ops[Boolean] {
  def mustBeTrue(): AlmValidation[Boolean] = funs.mustBeTrue(self)
  def mustBeFalse(): AlmValidation[Boolean] = funs.mustBeFalse(self)
  def mustBe(cond: => Boolean): AlmValidation[Boolean] =
    if (cond == self)
      self.success
    else
      ConstraintViolatedProblem(s"'${cond.toString}' was expected. The actual value was '${self.toString}'").failure
}

trait AlmStringConstraints extends Ops[String] {
  def mustFulfill(pred: String => Boolean, msg: String): AlmValidation[String] =
    if (pred(self)) self.success else ConstraintViolatedProblem(msg).failure

  def notEmptyOrWhitespace(): AlmValidation[String] =
    funs.notEmptyOrWhitespace(self)

  def constrainedTo(minLength: Option[Int], maxLength: Option[Int], emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, minLength, maxLength, emptyOrWhiteSpace)

  def minLength(minLength: Int): AlmValidation[String] =
    funs.stringConstrained(self, Some(minLength), None, true)

  def maxLength(maxLength: Int): AlmValidation[String] =
    funs.stringConstrained(self, None, Some(maxLength), true)

  def minMaxLength(minLength: Int, maxLength: Int): AlmValidation[String] =
    funs.stringConstrained(self, Some(minLength), Some(maxLength), true)

  def mustHaveLength(length: Int): AlmValidation[String] =
    funs.stringMustHaveLength(self, length)

  def allUpperCase(): AlmValidation[String] =
    funs.stringMustBeAllUpperCaseLetters(self)

  def allLowerCase(): AlmValidation[String] =
    funs.stringMustBeAllLowerCaseLetters(self)
}

trait AlmOptionStringConstraints extends Ops[Option[String]] {

  def notEmptyOrWhitespace(): AlmValidation[Option[String]] =
    onSome(x => funs.notEmptyOrWhitespace(x))

  def mustFulfill(pred: String => Boolean, msg: String): AlmValidation[Option[String]] =
    self match {
      case Some(str) => if (pred(str)) self.success else ConstraintViolatedProblem(msg).failure
      case None => self.success
    }

  def constrainedTo(minLength: Option[Int], maxLength: Option[Int]): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, minLength, maxLength, true))

  def minLength(minLength: Int): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, Some(minLength), None, true))

  def maxLength(maxLength: Int): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, None, Some(maxLength), true))

  def minMaxLength(minLength: Int, maxLength: Int): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, Some(minLength), Some(maxLength), true))

  def mustHaveLength(length: Int): AlmValidation[Option[String]] =
    onSome(x => funs.stringMustHaveLength(x, length))

  def allUpperCase(): AlmValidation[Option[String]] =
    onSome(x => funs.stringMustBeAllUpperCaseLetters(x))

  def allLowerCase(): AlmValidation[Option[String]] =
    onSome(x => funs.stringMustBeAllLowerCaseLetters(x))

  private def onSome(test: String => AlmValidation[String]): AlmValidation[Option[String]] =
    self match {
      case Some(str) => test(str).map(Some(_))
      case None => self.success
    }

}

trait AlmTraversableConstraints[T] extends Ops[Traversable[T]] {
  def notEmpty(): AlmValidation[Traversable[T]] = funs.collectionNotEmpty(self)
}

import language.implicitConversions

trait ToAlmValidationContraintsOps {
  implicit def FromBooleanToAlmBooleanConstraints(a: Boolean): AlmBooleanConstraints = new AlmBooleanConstraints { def self = a }
  implicit def FromStringToAlmStringConstraints(a: String): AlmStringConstraints = new AlmStringConstraints { def self = a }
  implicit def AlmOptionStringConstraints(a: Option[String]): AlmOptionStringConstraints = new AlmOptionStringConstraints { def self = a }
  implicit def AlmTraversableConstraints[T](a: Traversable[T]): AlmTraversableConstraints[T] = new AlmTraversableConstraints[T] { def self = a }

}
