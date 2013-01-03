package riftwarp.serialization.common

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

object Problems {
  def createDefaultRawDecomposer(aTypeDescriptor: TypeDescriptor): RawDecomposer = {
    new RawDecomposer {
      val typeDescriptor = aTypeDescriptor
      def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
        for {
          problem <- almhirt.almvalidation.funs.almCast[Problem](what)
          next <- into.addTypeDescriptor(typeDescriptor)
          next <- next.addString("message", problem.message)
          next <- next.addString("severity", problem.severity.toString())
          next <- next.addString("category", problem.category.toString())
          next <- next.addMap[String, Any]("args", problem.args)
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
    val inner = createDefaultRawDecomposer(aTypeDescriptor)
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

  type DefaultProblemCreator = ((String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])) => Problem
  
  private def recomposeBaseFields(from: Rematerializer): AlmValidation[(String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])] = {
    for {
      message <- from.getString("message")
      severity <- from.getString("severity").flatMap(Severity.fromString(_))
      category <- from.getString("category").flatMap(ProblemCategory.fromString(_))
      args <- from.getMap[String, Any]("args")
      cause <- from.tryGetComplexType[Problem]("cause").map(optV => optV.map(CauseIsProblem(_)))
    } yield (message, severity, category, args, cause)
  }

  def createDefaultRawRecomposer(aTypeDescriptor: TypeDescriptor, creator: DefaultProblemCreator): RawRecomposer = {
    new RawRecomposer {
      val typeDescriptor = aTypeDescriptor
      def recomposeRaw(from: Rematerializer): AlmValidation[Problem] = {
        recomposeBaseFields(from).map(x => creator(x))
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
  
  case class DefaultProblemDef(typeDescriptor: TypeDescriptor, creator: DefaultProblemCreator)
  val allCommonProblemDefs: List[DefaultProblemDef] =
    DefaultProblemDef(TypeDescriptor(classOf[Problem].getName), UnspecifiedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[UnspecifiedProblem].getName), UnspecifiedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[ExceptionCaughtProblem].getName), ExceptionCaughtProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[RegistrationProblem].getName), RegistrationProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[ServiceNotFoundProblem].getName), ServiceNotFoundProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[NoConnectionProblem].getName), NoConnectionProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[OperationTimedOutProblem].getName), OperationTimedOutProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[OperationAbortedProblem].getName), OperationAbortedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[IllegalOperationProblem].getName), IllegalOperationProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[OperationNotSupportedProblem].getName), OperationNotSupportedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[ArgumentProblem].getName), ArgumentProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[EmptyCollectionProblem].getName), EmptyCollectionProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[TypeCastProblem].getName), TypeCastProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[PersistenceProblem].getName), PersistenceProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[NotSupportedProblem].getName), NotSupportedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[NotFoundProblem].getName), NotFoundProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[KeyNotFoundProblem].getName), KeyNotFoundProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[ConstraintViolatedProblem].getName), ConstraintViolatedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[ParsingProblem].getName), ParsingProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[BadDataProblem].getName), BadDataProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[CollisionProblem].getName), CollisionProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[NotAuthorizedProblem].getName), NotAuthorizedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[NotAuthenticatedProblem].getName), NotAuthenticatedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[AlreadyExistsProblem].getName), AlreadyExistsProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[OperationCancelledProblem].getName), OperationCancelledProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[BusinessRuleViolatedProblem].getName), BusinessRuleViolatedProblem.tupled) :: 
    DefaultProblemDef(TypeDescriptor(classOf[LocaleNotSupportedProblem].getName), LocaleNotSupportedProblem.tupled) :: 
    //DefaultProblemDef(TypeDescriptor(classOf[ElementNotFoundProblem].getName), ElementNotFoundProblem.tupled) :: 
    Nil
    
    def registerAllDefaults(riftwarp: RiftWarp) {
      allCommonProblemDefs.foreach{pd =>
        val rawDecomposer = createDefaultRawDecomposer(pd.typeDescriptor) 
        riftwarp.barracks.addRawDecomposer(rawDecomposer)
        val rawRecomposer = createDefaultRawRecomposer(pd.typeDescriptor, pd.creator) 
        riftwarp.barracks.addRawRecomposer(rawRecomposer)
      }
  }
      
    def registerAllCommonProblems(riftwarp: RiftWarp) {
      registerAllDefaults(riftwarp)
      riftwarp.barracks.addDecomposer(createAggregateProblemDecomposer(TypeDescriptor(classOf[AggregateProblem].getName)))
      riftwarp.barracks.addRecomposer(createAggregateProblemRecomposer(TypeDescriptor(classOf[AggregateProblem].getName)))
    }
}