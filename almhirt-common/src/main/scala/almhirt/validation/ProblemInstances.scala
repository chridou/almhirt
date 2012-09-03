package almhirt.validation

import scalaz.{Semigroup, Show}

trait ProblemInstances {
  implicit def ToMBDSemiGroup: Semigroup[MultipleBadDataProblem] =
    new Semigroup[MultipleBadDataProblem] {
      def append(a: MultipleBadDataProblem, b: => MultipleBadDataProblem): MultipleBadDataProblem = a combineWith b
  }
  
  implicit def ToMultipleMappingSemiGroup: Semigroup[MultipleMappingProblem] =
    new Semigroup[MultipleMappingProblem] {
      def append(a: MultipleMappingProblem, b: => MultipleMappingProblem): MultipleMappingProblem = a combineWith b
  }

  implicit def ToManyBusinessRulesViolatedSemiGroup: Semigroup[ManyBusinessRulesViolatedProblem] =
    new Semigroup[ManyBusinessRulesViolatedProblem] {
      def append(a: ManyBusinessRulesViolatedProblem, b: => ManyBusinessRulesViolatedProblem): ManyBusinessRulesViolatedProblem = a combineWith b
  }

  implicit def ToAggregateProblemSemiGroup: Semigroup[AggregateProblem] =
    new Semigroup[AggregateProblem] {
      def append(a: AggregateProblem, b: => AggregateProblem): AggregateProblem = {
        val severity = a.severity and b.severity
        val category = a.category and b.category
        AggregateProblem("Multiple problems", severity = severity, category = category, causes = a.causes ++ b.causes)
      }
  }
  
  
  implicit def showsProblem: Show[Problem] = new Show[Problem] { override def shows(p: Problem) = standardShow(p) }
  implicit def showsUnspecifiedProblem: Show[UnspecifiedProblem] = new Show[UnspecifiedProblem] { override def shows(p: UnspecifiedProblem) = standardShow(p) }
  implicit def showsAggregateProblem: Show[AggregateProblem] = new Show[AggregateProblem] { override def shows(p: AggregateProblem) = standardShow(p) }
  implicit def showsMappingProblem: Show[MappingProblem] = new Show[MappingProblem] { override def shows(p: MappingProblem) = standardShow(p) }
  implicit def showsSecurityProblem: Show[SecurityProblem] = new Show[SecurityProblem] { override def shows(p: SecurityProblem) = standardShow(p) }
  implicit def showsBadDataProblem: Show[BadDataProblem] = new Show[BadDataProblem] { override def shows(p: BadDataProblem) = standardShow(p) }
  implicit def showsBusinessRuleProblem: Show[BusinessRuleProblem] = new Show[BusinessRuleProblem] { override def shows(p: BusinessRuleProblem) = standardShow(p) }
  implicit def showsRegistrationProblem: Show[RegistrationProblem] = new Show[RegistrationProblem] { override def shows(p: RegistrationProblem) = standardShow(p) }
  implicit def showsNoConnectionProblem: Show[NoConnectionProblem] = new Show[NoConnectionProblem] { override def shows(p: NoConnectionProblem) = standardShow(p) }
  implicit def showsOperationTimedOutProblem: Show[OperationTimedOutProblem] = new Show[OperationTimedOutProblem] { override def shows(p: OperationTimedOutProblem) = standardShow(p) }
  implicit def showsOperationAbortedProblem: Show[OperationAbortedProblem] = new Show[OperationAbortedProblem] { override def shows(p: OperationAbortedProblem) = standardShow(p) }
  implicit def showsIllegalOperationProblem: Show[IllegalOperationProblem] = new Show[IllegalOperationProblem] { override def shows(p: IllegalOperationProblem) = standardShow(p) }
  implicit def showsArgumentProblem: Show[ArgumentProblem] = new Show[ArgumentProblem] { override def shows(p: ArgumentProblem) = standardShow(p) }
  implicit def showsPersistenceProblem: Show[PersistenceProblem] = new Show[PersistenceProblem] { override def shows(p: PersistenceProblem) = standardShow(p) }
  implicit def showsSingleMappingProblem: Show[SingleMappingProblem] = new Show[SingleMappingProblem] { override def shows(p: SingleMappingProblem) = standardShow(p) }
  implicit def showsMultipleMappingProblem: Show[MultipleMappingProblem] = new Show[MultipleMappingProblem] { override def shows(p: MultipleMappingProblem) = standardShow(p) }
  implicit def showsNotFoundProblem: Show[NotFoundProblem] = new Show[NotFoundProblem] { override def shows(p: NotFoundProblem) = standardShow(p) }
  implicit def showsKeyNotFoundProblem: Show[KeyNotFoundProblem] = new Show[KeyNotFoundProblem] { override def shows(p: KeyNotFoundProblem) = standardShow(p) }
  implicit def showsConstraintViolatedProblem: Show[ConstraintViolatedProblem] = new Show[ConstraintViolatedProblem] { override def shows(p: ConstraintViolatedProblem) = standardShow(p) }
  implicit def showsParsingProblem: Show[ParsingProblem] = new Show[ParsingProblem]  { override def shows(p: ParsingProblem) = standardShow(p) }
  implicit def showsSingleBadDataProblem: Show[SingleBadDataProblem] = new Show[SingleBadDataProblem] { override def shows(p: SingleBadDataProblem) = standardShow(p) }
  implicit def showsMultipleBadDataProblem: Show[MultipleBadDataProblem] = new Show[MultipleBadDataProblem] { override def shows(p: MultipleBadDataProblem) = standardShow(p) }
  implicit def showsCollisionProblem: Show[CollisionProblem] = new Show[CollisionProblem] { override def shows(p: CollisionProblem) = standardShow(p) }
  implicit def showsNotAuthorizedProblem: Show[NotAuthorizedProblem] = new Show[NotAuthorizedProblem] { override def shows(p: NotAuthorizedProblem) = standardShow(p) }
  implicit def showsNotAuthenticatedProblem: Show[NotAuthenticatedProblem] = new Show[NotAuthenticatedProblem] { override def shows(p: NotAuthenticatedProblem) = standardShow(p) }
  implicit def showsAlreadyExistsProblem: Show[AlreadyExistsProblem] = new Show[AlreadyExistsProblem] { override def shows(p: AlreadyExistsProblem) = standardShow(p) }
  implicit def showsOperationCancelledProblem: Show[OperationCancelledProblem] = new Show[OperationCancelledProblem] { override def shows(p: OperationCancelledProblem) = standardShow(p) }
  implicit def showsBusinessRuleViolatedProblem: Show[BusinessRuleViolatedProblem] = new Show[BusinessRuleViolatedProblem] { override def shows(p: BusinessRuleViolatedProblem) = standardShow(p) }
  implicit def showsManyBusinessRulesViolatedProblem: Show[ManyBusinessRulesViolatedProblem] = new Show[ManyBusinessRulesViolatedProblem] { override def shows(p: ManyBusinessRulesViolatedProblem) = standardShow(p) }

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
  
}

object ProblemInstances extends ProblemInstances