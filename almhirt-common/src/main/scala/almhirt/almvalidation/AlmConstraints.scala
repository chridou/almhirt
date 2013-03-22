package almhirt.almvalidation

import scalaz.syntax.Ops
import scalaz.syntax.validation._
import almhirt.common._

trait AlmStringConstraints extends Ops[String] {
  def mustFulfill(pred: String => Boolean, msg: String): AlmValidation[String] =
    if (pred(self)) self.success else ConstraintViolatedProblem(msg).failure

  def notEmptyOrWhitespace(): AlmValidation[String] =
    funs.notEmptyOrWhitespace(self)
    
  def constrained(minLength: Option[Int], maxLength: Option[Int], emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, minLength, maxLength, emptyOrWhiteSpace)

  def constrainedToMinLength(minLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, Some(minLength), None, emptyOrWhiteSpace)

  def constrainedToMaxLength(maxLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, None, Some(maxLength), emptyOrWhiteSpace)

  def constrainedToMinAndMaxLength(minLength: Int, maxLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    funs.stringConstrained(self, Some(minLength), Some(maxLength), emptyOrWhiteSpace)

  
}

trait AlmOptionStringConstraints extends Ops[Option[String]] {
    
  def notEmptyOrWhitespace(): AlmValidation[Option[String]] =
    onSome(x => funs.notEmptyOrWhitespace(x))
  
  def mustFulfill(pred: String => Boolean, msg: String): AlmValidation[Option[String]] =
    self match {
      case Some(str) => if (pred(str)) self.success else ConstraintViolatedProblem(msg).failure
      case None => self.success
    }

  def constrained(minLength: Option[Int], maxLength: Option[Int], emptyOrWhiteSpace: Boolean = false): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, minLength, maxLength, emptyOrWhiteSpace))

  def constrainedToMinLength(minLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, Some(minLength), None, emptyOrWhiteSpace))

  def constrainedToMaxLength(maxLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, None, Some(maxLength), emptyOrWhiteSpace))

  def constrainedToMinAndMaxLength(minLength: Int, maxLength: Int, emptyOrWhiteSpace: Boolean = false): AlmValidation[Option[String]] =
    onSome(x => funs.stringConstrained(x, Some(minLength), Some(maxLength), emptyOrWhiteSpace))

  private def onSome(test: String => AlmValidation[String]): AlmValidation[Option[String]] =
    self match {
      case Some(str) => test(str).map(Some(_))
      case None => self.success
    }
    
}

import language.implicitConversions

trait ToAlmValidationContraintsOps {
    implicit def FromStringToAlmStringConstraints(a: String): AlmStringConstraints = new AlmStringConstraints { def self = a }
    implicit def AlmOptionStringConstraints(a: Option[String]): AlmOptionStringConstraints = new AlmOptionStringConstraints { def self = a }

}
