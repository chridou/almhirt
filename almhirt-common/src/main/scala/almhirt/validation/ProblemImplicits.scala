package almhirt.validation

import scalaz._
import Scalaz._
import Problem._

trait ProblemImplicits {
  implicit def toSeverityMonoid: Monoid[Severity] =
    new Monoid[Severity] {
      def append(a: Severity, b: => Severity): Severity = a and b
      val zero = NoProblem
    }

  implicit def toProbelCategoryMonoid: Monoid[ProblemCategory] =
    new Monoid[ProblemCategory] {
      def append(a: ProblemCategory, b: => ProblemCategory): ProblemCategory = a and b
      val zero = ApplicationProblem
    }
  
  implicit def toMBDSemiGroup: Semigroup[MultipleBadDataProblem] =
    new Semigroup[MultipleBadDataProblem] {
      def append(a: MultipleBadDataProblem, b: => MultipleBadDataProblem): MultipleBadDataProblem = a combineWith b
  }
  
  implicit def toMultipleMappingSemiGroup: Semigroup[MultipleMappingProblem] =
    new Semigroup[MultipleMappingProblem] {
      def append(a: MultipleMappingProblem, b: => MultipleMappingProblem): MultipleMappingProblem = a combineWith b
  }

  implicit def toManyBusinessRulesViolatedSemiGroup: Semigroup[ManyBusinessRulesViolatedProblem] =
    new Semigroup[ManyBusinessRulesViolatedProblem] {
      def append(a: ManyBusinessRulesViolatedProblem, b: => ManyBusinessRulesViolatedProblem): ManyBusinessRulesViolatedProblem = a combineWith b
  }
  
  implicit def nelProblemtoNelProblemW(probs: NonEmptyList[Problem]) = new NelProblemW(probs)
  final class NelProblemW(nel: NonEmptyList[Problem]) {
    def aggregate(msg: String): Problem = {
      val severity = nel.map(_.severity).concatenate
      if(nel.list.exists(p => p.isSystemProblem))
        UnspecifiedProblem(msg, severity = severity, category = SystemProblem, causes = nel.list)
      else
        UnspecifiedProblem(msg, severity = severity, category = ApplicationProblem, causes = nel.list)
    }
    def aggregate(): Problem = aggregate("One or more problems. See causes.")
  }
  
  private def standardShow(p: Problem): String = {
      val builder = new StringBuilder()
      builder.append("Message: %s\n".format(p.message))
      builder.append("Severity: %s\n".format(p.severity))
      p.exception.foreach(exn => builder.append("Exception: %s\n".format(exn.toString)))
      p.exception.foreach(exn => builder.append("Stacktrace:\n%s\n".format(exn.getStackTraceString)))
      builder.append("Arguments: %s\n".format(p.args))
      builder.append("Causes:\n%s\n".format(p.causes))
      builder.result
    }
  
  implicit object showsProblem extends Show[Problem] { override def shows(p: Problem) = standardShow(p) }
  implicit object showsMappingProblem extends Show[MappingProblem] { override def shows(p: MappingProblem) = standardShow(p) }
  implicit object showsSecurityProblem extends Show[SecurityProblem] { override def shows(p: SecurityProblem) = standardShow(p) }
  implicit object showsBadDataProblem extends Show[BadDataProblem] { override def shows(p: BadDataProblem) = standardShow(p) }
  implicit object showsBusinessRuleProblem extends Show[BusinessRuleProblem] { override def shows(p: BusinessRuleProblem) = standardShow(p) }
  implicit object showsUnspecifiedProblem extends Show[UnspecifiedProblem] { override def shows(p: UnspecifiedProblem) = standardShow(p) }
  implicit object showsRegistrationProblem extends Show[RegistrationProblem] { override def shows(p: RegistrationProblem) = standardShow(p) }
  implicit object showsNoConnectionProblem extends Show[NoConnectionProblem] { override def shows(p: NoConnectionProblem) = standardShow(p) }
  implicit object showsOperationTimedOutProblem extends Show[OperationTimedOutProblem] { override def shows(p: OperationTimedOutProblem) = standardShow(p) }
  implicit object showsOperationAbortedProblem extends Show[OperationAbortedProblem] { override def shows(p: OperationAbortedProblem) = standardShow(p) }
  implicit object showsIllegalOperationProblem extends Show[IllegalOperationProblem] { override def shows(p: IllegalOperationProblem) = standardShow(p) }
  implicit object showsArgumentProblem extends Show[ArgumentProblem] { override def shows(p: ArgumentProblem) = standardShow(p) }
  implicit object showsPersistenceProblem extends Show[PersistenceProblem] { override def shows(p: PersistenceProblem) = standardShow(p) }
  implicit object showsSingleMappingProblem extends Show[SingleMappingProblem] { override def shows(p: SingleMappingProblem) = standardShow(p) }
  implicit object showsMultipleMappingProblem extends Show[MultipleMappingProblem] { override def shows(p: MultipleMappingProblem) = standardShow(p) }
  implicit object showsNotFoundProblem extends Show[NotFoundProblem] { override def shows(p: NotFoundProblem) = standardShow(p) }
  implicit object showsKeyNotFoundProblem extends Show[KeyNotFoundProblem] { override def shows(p: KeyNotFoundProblem) = standardShow(p) }
  implicit object showsConstraintViolatedProblem extends Show[ConstraintViolatedProblem] { override def shows(p: ConstraintViolatedProblem) = standardShow(p) }
  implicit object showsParsingProblem extends Show[ParsingProblem] { override def shows(p: ParsingProblem) = standardShow(p) }
  implicit object showsSingleBadDataProblem extends Show[SingleBadDataProblem] { override def shows(p: SingleBadDataProblem) = standardShow(p) }
  implicit object showsMultipleBadDataProblem extends Show[MultipleBadDataProblem] { override def shows(p: MultipleBadDataProblem) = standardShow(p) }
  implicit object showsCollisionProblem extends Show[CollisionProblem] { override def shows(p: CollisionProblem) = standardShow(p) }
  implicit object showsNotAuthorizedProblem extends Show[NotAuthorizedProblem] { override def shows(p: NotAuthorizedProblem) = standardShow(p) }
  implicit object showsNotAuthenticatedProblem extends Show[NotAuthenticatedProblem] { override def shows(p: NotAuthenticatedProblem) = standardShow(p) }
  implicit object showsAlreadyExistsProblem extends Show[AlreadyExistsProblem] { override def shows(p: AlreadyExistsProblem) = standardShow(p) }
  implicit object showsOperationCancelledProblem extends Show[OperationCancelledProblem] { override def shows(p: OperationCancelledProblem) = standardShow(p) }
  implicit object showsBusinessRuleViolatedProblem extends Show[BusinessRuleViolatedProblem] { override def shows(p: BusinessRuleViolatedProblem) = standardShow(p) }
  implicit object showsManyBusinessRulesViolatedProblem extends Show[ManyBusinessRulesViolatedProblem] { override def shows(p: ManyBusinessRulesViolatedProblem) = standardShow(p) }
}
