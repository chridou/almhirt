package riftwarp.serialization.common

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

object Problems {
  def createDefaultDecomposer[T <: Problem](aTypeDescriptor: TypeDescriptor): Decomposer[T] = {
    new Decomposer[T] {
      val typeDescriptor = aTypeDescriptor
      def decompose[TDimension <: RiftDimension](what: T)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
        for {
          problem <- almhirt.almvalidation.funs.almCast[Problem](what)
          next <- into.addTypeDescriptor(typeDescriptor)
          next <- next.addString("message", problem.message)
          next <- next.addString("severity", problem.severity.toString())
          next <- next.addString("category", problem.category.toString())
          next <- next.addMapSkippingUnknownValues[String, Any]("args", problem.args)
          next <- option.cata(problem.cause)(
            cause => cause match {
              case CauseIsThrowable(_) => next.success
              case CauseIsProblem(prob) => next.addComplexType("cause", prob)
            },
            next.success)
        } yield next
    }
  }

  def createAggregateProblemDecomposer(aTypeDescriptor: TypeDescriptor): Decomposer[AggregateProblem] = {
    val inner = createDefaultDecomposer[Problem](aTypeDescriptor)
    new Decomposer[AggregateProblem] {
      val typeDescriptor = aTypeDescriptor
      def decompose[TDimension <: RiftDimension](what: AggregateProblem)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =

        for {
          problem <- almhirt.almvalidation.funs.almCast[AggregateProblem](what)
          defaults <- inner.decomposeRaw(what)(into)
          additional <- defaults.addComplexMALoose("problems", problem.problems)
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
      cause <- from.tryGetComplexType[Problem]("cause").map(optV => optV.map(CauseIsProblem(_)))
    } yield (message, severity, category, args, cause)
  }

  def createDefaultRecomposer[T <: Problem](aTypeDescriptor: TypeDescriptor, creator: DefaultProblemCreator[T]): Recomposer[T] = {
    new Recomposer[T] {
      val typeDescriptor = aTypeDescriptor
      def recompose(from: Rematerializer): AlmValidation[T] = {
        recomposeBaseFields(from).map(x => creator(x).asInstanceOf[T])
      }
    }
  }

  def createAggregateProblemRecomposer(aTypeDescriptor: TypeDescriptor): Recomposer[AggregateProblem] =
    new Recomposer[AggregateProblem] {
      val typeDescriptor = aTypeDescriptor
      def recompose(from: Rematerializer): AlmValidation[AggregateProblem] = {
        for {
          baseFields <- recomposeBaseFields(from)
          problems <- from.getComplexMALoose[List, Problem]("problems")
        } yield AggregateProblem(baseFields._1, baseFields._2, baseFields._3, baseFields._4, baseFields._5, problems)
      }
    }
  
  def createDefaultDecomposerAndRecomposer[T <: Problem](typeDescriptor: TypeDescriptor, creator: DefaultProblemCreator[T]) =
    (createDefaultDecomposer[T](typeDescriptor), createDefaultRecomposer[T](typeDescriptor, creator))
  
  def createAndRegisterDefaultDecomposerAndRecomposer[T <: Problem](riftwarp: RiftWarp)(typeDescriptor: TypeDescriptor, creator: DefaultProblemCreator[T]) {
    val (decomposer, recomposer) = createDefaultDecomposerAndRecomposer(typeDescriptor, creator)
    riftwarp.barracks.addDecomposer(decomposer)
    riftwarp.barracks.addRecomposer(recomposer)
  }  

  def createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp: RiftWarp) {
    createAndRegisterDefaultDecomposerAndRecomposer[Problem](riftwarp)(TypeDescriptor(classOf[Problem].getName), UnspecifiedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[UnspecifiedProblem](riftwarp)(TypeDescriptor(classOf[UnspecifiedProblem].getName), UnspecifiedProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ExceptionCaughtProblem](riftwarp)(TypeDescriptor(classOf[ExceptionCaughtProblem].getName), ExceptionCaughtProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[RegistrationProblem](riftwarp)(TypeDescriptor(classOf[RegistrationProblem].getName), RegistrationProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[ServiceNotFoundProblem](riftwarp)(TypeDescriptor(classOf[ServiceNotFoundProblem].getName), ServiceNotFoundProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NoConnectionProblem](riftwarp)(TypeDescriptor(classOf[NoConnectionProblem].getName), NoConnectionProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[OperationTimedOutProblem](riftwarp)(TypeDescriptor(classOf[OperationTimedOutProblem].getName), OperationTimedOutProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationAbortedProblem](riftwarp)(TypeDescriptor(classOf[OperationAbortedProblem].getName), OperationAbortedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[IllegalOperationProblem](riftwarp)(TypeDescriptor(classOf[IllegalOperationProblem].getName), IllegalOperationProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationNotSupportedProblem](riftwarp)(TypeDescriptor(classOf[OperationNotSupportedProblem].getName), OperationNotSupportedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[ArgumentProblem](riftwarp)(TypeDescriptor(classOf[ArgumentProblem].getName), ArgumentProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[EmptyCollectionProblem](riftwarp)(TypeDescriptor(classOf[EmptyCollectionProblem].getName), EmptyCollectionProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[TypeCastProblem](riftwarp)(TypeDescriptor(classOf[TypeCastProblem].getName), TypeCastProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[PersistenceProblem](riftwarp)(TypeDescriptor(classOf[PersistenceProblem].getName), PersistenceProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[NotSupportedProblem](riftwarp)(TypeDescriptor(classOf[NotSupportedProblem].getName), NotSupportedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[MappingProblem](riftwarp)(TypeDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NotFoundProblem](riftwarp)(TypeDescriptor(classOf[NotFoundProblem].getName), NotFoundProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[KeyNotFoundProblem](riftwarp)(TypeDescriptor(classOf[KeyNotFoundProblem].getName), KeyNotFoundProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[ConstraintViolatedProblem](riftwarp)(TypeDescriptor(classOf[ConstraintViolatedProblem].getName), ConstraintViolatedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[ParsingProblem](riftwarp)(TypeDescriptor(classOf[ParsingProblem].getName), ParsingProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[BadDataProblem](riftwarp)(TypeDescriptor(classOf[BadDataProblem].getName), BadDataProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[CollisionProblem](riftwarp)(TypeDescriptor(classOf[CollisionProblem].getName), CollisionProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthorizedProblem](riftwarp)(TypeDescriptor(classOf[NotAuthorizedProblem].getName), NotAuthorizedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthenticatedProblem](riftwarp)(TypeDescriptor(classOf[NotAuthenticatedProblem].getName), NotAuthenticatedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[AlreadyExistsProblem](riftwarp)(TypeDescriptor(classOf[AlreadyExistsProblem].getName), AlreadyExistsProblem.tupled)
    createAndRegisterDefaultDecomposerAndRecomposer[OperationCancelledProblem](riftwarp)(TypeDescriptor(classOf[OperationCancelledProblem].getName), OperationCancelledProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[BusinessRuleViolatedProblem](riftwarp)(TypeDescriptor(classOf[BusinessRuleViolatedProblem].getName), BusinessRuleViolatedProblem.tupled) 
    createAndRegisterDefaultDecomposerAndRecomposer[LocaleNotSupportedProblem](riftwarp)(TypeDescriptor(classOf[LocaleNotSupportedProblem].getName), LocaleNotSupportedProblem.tupled) 
    //createAndRegisterDefaultDecomposerAndRecomposer[ElementNotFoundProblem](riftwarp)(TypeDescriptor(classOf[ElementNotFoundProblem].getName), LocaleNotSupportedProblem.tupled) 
  }
  
  def registerAllCommonProblems(riftwarp: RiftWarp) {
      createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp)
      riftwarp.barracks.addDecomposer(createAggregateProblemDecomposer(TypeDescriptor(classOf[AggregateProblem].getName)))
      riftwarp.barracks.addRecomposer(createAggregateProblemRecomposer(TypeDescriptor(classOf[AggregateProblem].getName)))
    }
}