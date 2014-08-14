package almhirt.almvalidation

import scalaz.syntax.Ops
import scalaz.syntax.validation._
import almhirt.common._

trait AlmBooleanConstraints extends Ops[Boolean] {
  /** Same as [[almhirt.almvalidation.AlmConstraintsFuns.mustBeTrue */
  def mustBeTrue(): AlmValidation[Boolean] = funs.mustBeTrue(self)
  
  /** Same as  [[almhirt.almvalidation.AlmConstraintsFuns.mustBeFalse */
  def mustBeFalse(): AlmValidation[Boolean] = funs.mustBeFalse(self)
  
  /** Completes with a [[almhirt.problem.problemtypes.ConstraintViolatedProblem]] if cond evaluates to false 
   *  
   * @param cond Something that evaluates to true or false
   */
  def mustBe(cond: ⇒ Boolean): AlmValidation[Boolean] =
    if (cond == self)
      self.success
    else
      ConstraintViolatedProblem(s"'${cond.toString}' was expected. The actual value was '${self.toString}'").failure
}

trait AlmStringConstraints extends Ops[String] {
  /** Completes with a [[almhirt.problem.problemtypes.ConstraintViolatedProblem]] if the predicate is not met. 
   *  
   * @param pred The predicate
   * @param createMessage Map the input of the Sting to an error message (or use the default)
   */
  def mustFulfill(pred: String ⇒ Boolean, createMessage: String ⇒ String = str ⇒ s"""Predicate not met for value "$str""""): AlmValidation[String] =
    if (pred(self)) self.success else ConstraintViolatedProblem(createMessage(self)).failure

  def notEmptyOrWhitespace(): AlmValidation[String] =
    funs.notEmptyOrWhitespace(self)

  def constrainedTo(minLength: Option[Int], maxLength: Option[Int], emptyOrWhiteSpaceAllowed: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, minLength, maxLength, emptyOrWhiteSpaceAllowed)

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
    onSome(x ⇒ funs.notEmptyOrWhitespace(x))

  /** Completes with a [[almhirt.problem.problemtypes.ConstraintViolatedProblem]] if the predicate is not met for the option's value. 
   *  
   * @param pred The predicate
   * @param createMessage Map the input of the Sting to an error message (or use the default)
   */
  def mustFulfill(pred: String ⇒ Boolean, createMessage: String ⇒ String = str ⇒ s"""Predicate not met for value "$str""""): AlmValidation[Option[String]] =
    self match {
      case Some(str) ⇒ if (pred(str)) self.success else ConstraintViolatedProblem(createMessage(str)).failure
      case None ⇒ self.success
    }

  def constrainedTo(minLength: Option[Int], maxLength: Option[Int]): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringConstrained(x, minLength, maxLength, true))

  def minLength(minLength: Int): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringConstrained(x, Some(minLength), None, true))

  def maxLength(maxLength: Int): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringConstrained(x, None, Some(maxLength), true))

  def minMaxLength(minLength: Int, maxLength: Int): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringConstrained(x, Some(minLength), Some(maxLength), true))

  def mustHaveLength(length: Int): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringMustHaveLength(x, length))

  def allUpperCase(): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringMustBeAllUpperCaseLetters(x))

  def allLowerCase(): AlmValidation[Option[String]] =
    onSome(x ⇒ funs.stringMustBeAllLowerCaseLetters(x))

  private def onSome(test: String ⇒ AlmValidation[String]): AlmValidation[Option[String]] =
    self match {
      case Some(str) ⇒ test(str).map(Some(_))
      case None ⇒ self.success
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
