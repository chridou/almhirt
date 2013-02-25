package riftwarp.serialization.common

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

object Problems {
  def createDefaultDecomposer[T <: Problem](aRiftDescriptor: RiftDescriptor): Decomposer[T] = {
    new Decomposer[T] {
      val riftDescriptor = aRiftDescriptor
      val alternativeRiftDescriptors = Nil
      def decompose[TDimension <: RiftDimension](what: T, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
        into.addRiftDescriptor(riftDescriptor)
          .addString("message", what.message)
          .addString("severity", what.severity.toString())
          .addString("category", what.category.toString())
          .addMapSkippingUnknownValues[String, Any]("args", what.args).flatMap(
            _.addOptionalComplexSelective("cause", ProblemCauseDecomposer, what.cause))
    }
  }

  def createAggregateProblemDecomposer(aRiftDescriptor: RiftDescriptor): Decomposer[AggregateProblem] = {
    val inner = createDefaultDecomposer[Problem](aRiftDescriptor)
    new Decomposer[AggregateProblem] {
      val riftDescriptor = aRiftDescriptor
      val alternativeRiftDescriptors = Nil
      def decompose[TDimension <: RiftDimension](what: AggregateProblem, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
        for {
          defaults <- inner.decomposeRaw(what, into)
          additional <- defaults.addComplexMALoose("problems", what.problems)
        } yield additional
    }
  }

  type DefaultProblemCreator[T <: Problem] = ((String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])) => T

  private def recomposeBaseFields(from: Rematerializer): AlmValidation[(String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])] = {
    for {
      message <- from.getString("message")
      severity <- from.getString("severity").flatMap(Severity.fromString(_))
      category <- from.getString("category").flatMap(ProblemCategory.fromString(_))
      args <- from.getMap[String, Any]("args")
      cause <- from.tryGetComplexType("cause", ProblemCauseRecomposer)
    } yield (message, severity, category, args, cause)
  }

  def createDefaultRecomposer[T <: Problem](aRiftDescriptor: RiftDescriptor, creator: DefaultProblemCreator[T]): Recomposer[T] = {
    new Recomposer[T] {
      val riftDescriptor = aRiftDescriptor
      val alternativeRiftDescriptors = Nil
      def recompose(from: Rematerializer): AlmValidation[T] = {
        recomposeBaseFields(from).map(x => creator(x).asInstanceOf[T])
      }
    }
  }

  def createAggregateProblemRecomposer(aRiftDescriptor: RiftDescriptor): Recomposer[AggregateProblem] =
    new Recomposer[AggregateProblem] {
      val riftDescriptor = aRiftDescriptor
      val alternativeRiftDescriptors = Nil
      def recompose(from: Rematerializer): AlmValidation[AggregateProblem] = {
        for {
          baseFields <- recomposeBaseFields(from)
          problems <- from.getComplexMALoose[List, Problem]("problems")
        } yield AggregateProblem(baseFields._1, baseFields._2, baseFields._3, baseFields._4, baseFields._5, problems)
      }
    }

  def createDefaultDecomposerAndRecomposer[T <: Problem](riftDescriptor: RiftDescriptor, creator: DefaultProblemCreator[T]) =
    (createDefaultDecomposer[T](riftDescriptor), createDefaultRecomposer[T](riftDescriptor, creator))

  def createAndRegisterDefaultDecomposerAndRecomposer[T <: Problem](riftwarp: RiftWarp)(riftDescriptor: RiftDescriptor, creator: DefaultProblemCreator[T]) {
    val (decomposer, recomposer) = createDefaultDecomposerAndRecomposer(riftDescriptor, creator)
    riftwarp.barracks.addDecomposer(decomposer)
    riftwarp.barracks.addRecomposer(recomposer)
  }

  def createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp: RiftWarp) {
    createAndRegisterDefaultDecomposerAndRecomposer[Problem](riftwarp)(RiftDescriptor(classOf[Problem].getName), UnspecifiedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[UnspecifiedProblem](riftwarp)(RiftDescriptor(classOf[UnspecifiedProblem].getName), UnspecifiedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ExceptionCaughtProblem](riftwarp)(RiftDescriptor(classOf[ExceptionCaughtProblem].getName), ExceptionCaughtProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[RegistrationProblem](riftwarp)(RiftDescriptor(classOf[RegistrationProblem].getName), RegistrationProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ServiceNotFoundProblem](riftwarp)(RiftDescriptor(classOf[ServiceNotFoundProblem].getName), ServiceNotFoundProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NoConnectionProblem](riftwarp)(RiftDescriptor(classOf[NoConnectionProblem].getName), NoConnectionProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationTimedOutProblem](riftwarp)(RiftDescriptor(classOf[OperationTimedOutProblem].getName), OperationTimedOutProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationAbortedProblem](riftwarp)(RiftDescriptor(classOf[OperationAbortedProblem].getName), OperationAbortedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[IllegalOperationProblem](riftwarp)(RiftDescriptor(classOf[IllegalOperationProblem].getName), IllegalOperationProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationNotSupportedProblem](riftwarp)(RiftDescriptor(classOf[OperationNotSupportedProblem].getName), OperationNotSupportedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ArgumentProblem](riftwarp)(RiftDescriptor(classOf[ArgumentProblem].getName), ArgumentProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[EmptyCollectionProblem](riftwarp)(RiftDescriptor(classOf[EmptyCollectionProblem].getName), EmptyCollectionProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[InvalidCastProblem](riftwarp)(RiftDescriptor(classOf[InvalidCastProblem].getName), InvalidCastProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[PersistenceProblem](riftwarp)(RiftDescriptor(classOf[PersistenceProblem].getName), PersistenceProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NotSupportedProblem](riftwarp)(RiftDescriptor(classOf[NotSupportedProblem].getName), NotSupportedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[MappingProblem](riftwarp)(RiftDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[MappingProblem](riftwarp)(RiftDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[SerializationProblem](riftwarp)(RiftDescriptor(classOf[SerializationProblem].getName), SerializationProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[StartupProblem](riftwarp)(RiftDescriptor(classOf[StartupProblem].getName), StartupProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[IndexOutOfBoundsProblem](riftwarp)(RiftDescriptor(classOf[IndexOutOfBoundsProblem].getName), IndexOutOfBoundsProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[KeyNotFoundProblem](riftwarp)(RiftDescriptor(classOf[KeyNotFoundProblem].getName), KeyNotFoundProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ConstraintViolatedProblem](riftwarp)(RiftDescriptor(classOf[ConstraintViolatedProblem].getName), ConstraintViolatedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ParsingProblem](riftwarp)(RiftDescriptor(classOf[ParsingProblem].getName), ParsingProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[BadDataProblem](riftwarp)(RiftDescriptor(classOf[BadDataProblem].getName), BadDataProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[CollisionProblem](riftwarp)(RiftDescriptor(classOf[CollisionProblem].getName), CollisionProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthorizedProblem](riftwarp)(RiftDescriptor(classOf[NotAuthorizedProblem].getName), NotAuthorizedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthenticatedProblem](riftwarp)(RiftDescriptor(classOf[NotAuthenticatedProblem].getName), NotAuthenticatedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[AlreadyExistsProblem](riftwarp)(RiftDescriptor(classOf[AlreadyExistsProblem].getName), AlreadyExistsProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationCancelledProblem](riftwarp)(RiftDescriptor(classOf[OperationCancelledProblem].getName), OperationCancelledProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[BusinessRuleViolatedProblem](riftwarp)(RiftDescriptor(classOf[BusinessRuleViolatedProblem].getName), BusinessRuleViolatedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[LocaleNotSupportedProblem](riftwarp)(RiftDescriptor(classOf[LocaleNotSupportedProblem].getName), LocaleNotSupportedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NoSuchElementProblem](riftwarp)(RiftDescriptor(classOf[NoSuchElementProblem].getName), NoSuchElementProblem.tupled)

    createAndRegisterDefaultDecomposerAndRecomposer[RiftWarpProblem](riftwarp)(RiftDescriptor(classOf[RiftWarpProblem].getName), RiftWarpProblem.tupled)
  }

  def registerAllCommonProblems(riftwarp: RiftWarp) {
    createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp)
    riftwarp.barracks.addDecomposer(createAggregateProblemDecomposer(RiftDescriptor(classOf[AggregateProblem].getName)))
    riftwarp.barracks.addRecomposer(createAggregateProblemRecomposer(RiftDescriptor(classOf[AggregateProblem].getName)))
  }
}