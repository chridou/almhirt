package riftwarp.serialization.common

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.std.kit._

object Problems {
  type DefaultProblemCreator[T <: Problem] = ((String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])) => T

  def createDefaultPacker[T <: Problem](aWarpDescriptor: WarpDescriptor): WarpPacker[T] with RegisterableWarpPacker = {
    new WarpPacker[T] with RegisterableWarpPacker {
      val warpDescriptor = aWarpDescriptor
      val alternativeWarpDescriptors = Nil
      def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        warpDescriptor ~>
          P("message", what.message) ~>
          P("severity", what.severity.toString()) ~>
          P("category", what.category.toString()) ~>
          MLookUpForgiving[String, Any]("args", what.args) ~>
          WithOpt("cause", what.cause, ProblemCausePacker)
    }
  }

  def createDefaultUnpacker[T <: Problem](aWarpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]): RegisterableWarpUnpacker[T] = {
    new RegisterableWarpUnpacker[T] {
      val warpDescriptor = aWarpDescriptor
      val alternativeWarpDescriptors = Nil
      def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] = {
        withFastLookUp(from) { lookup =>
          for {
            message <- lookup.getAs[String]("message")
            severity <- lookup.getAs[String]("severity").flatMap(Severity.fromString(_))
            category <- lookup.getAs[String]("category").flatMap(ProblemCategory.fromString(_))
            args <- lookup.getAssocs[String]("args").map(_.toMap)
            cause <- lookup.tryGetWith("cause", ProblemCauseUnpacker)
          } yield creator(message, severity, category, args, cause)
        }
      }
    }
  }

  def createAggregateProblemPacker(aWarpDescriptor: WarpDescriptor): WarpPacker[AggregateProblem] with RegisterableWarpPacker = {
    new WarpPacker[AggregateProblem] with RegisterableWarpPacker {
      val warpDescriptor = aWarpDescriptor
      val alternativeWarpDescriptors = Nil
      def pack(what: AggregateProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        warpDescriptor ~>
          P("message", what.message) ~>
          P("severity", what.severity.toString()) ~>
          P("category", what.category.toString()) ~>
          MLookUpForgiving[String, Any]("args", what.args) ~>
          WithOpt("cause", what.cause, ProblemCausePacker) ~>
          CLookUp("problems", what.problems)
    }
  }

  def createAggregateProblemUnpacker(aWarpDescriptor: WarpDescriptor): RegisterableWarpUnpacker[AggregateProblem] = {
    new RegisterableWarpUnpacker[AggregateProblem] {
      val warpDescriptor = aWarpDescriptor
      val alternativeWarpDescriptors = Nil
      def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[AggregateProblem] = {
        withFastLookUp(from) { lookup =>
          for {
            message <- lookup.getAs[String]("message")
            severity <- lookup.getAs[String]("severity").flatMap(Severity.fromString(_))
            category <- lookup.getAs[String]("category").flatMap(ProblemCategory.fromString(_))
            args <- lookup.getAssocs[String]("args").map(_.toMap)
            cause <- lookup.tryGetWith("cause", ProblemCauseUnpacker)
            problems <- lookup.getManyTyped[Problem]("problems").map(_.toList)
          } yield AggregateProblem(message, severity, category, args, cause, problems)
        }
      }
    }
  }

  def createDefaultDecomposerAndRecomposer[T <: Problem](warpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]) =
    (createDefaultPacker[T](warpDescriptor), createDefaultUnpacker[T](warpDescriptor, creator))

  def createAndRegisterDefaultPackerAndUnpacker[T <: Problem](packers: WarpPackers, unpackers: WarpUnpackers)(warpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]) {
    val (packer, unpacker) = createDefaultDecomposerAndRecomposer(warpDescriptor, creator)
    packers.addTyped[T](packer)
    unpackers.addTyped[T](unpacker)
  }

  def createAndRegisterAllDefaultDecomposersAndRecomposers(packers: WarpPackers, unpackers: WarpUnpackers) {
    createAndRegisterDefaultPackerAndUnpacker[Problem](packers, unpackers)(WarpDescriptor(classOf[Problem].getName), UnspecifiedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[UnspecifiedProblem](packers, unpackers)(WarpDescriptor(classOf[UnspecifiedProblem].getName), UnspecifiedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ExceptionCaughtProblem](packers, unpackers)(WarpDescriptor(classOf[ExceptionCaughtProblem].getName), ExceptionCaughtProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[RegistrationProblem](packers, unpackers)(WarpDescriptor(classOf[RegistrationProblem].getName), RegistrationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ServiceNotFoundProblem](packers, unpackers)(WarpDescriptor(classOf[ServiceNotFoundProblem].getName), ServiceNotFoundProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NoConnectionProblem](packers, unpackers)(WarpDescriptor(classOf[NoConnectionProblem].getName), NoConnectionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationTimedOutProblem](packers, unpackers)(WarpDescriptor(classOf[OperationTimedOutProblem].getName), OperationTimedOutProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationAbortedProblem](packers, unpackers)(WarpDescriptor(classOf[OperationAbortedProblem].getName), OperationAbortedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[IllegalOperationProblem](packers, unpackers)(WarpDescriptor(classOf[IllegalOperationProblem].getName), IllegalOperationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationNotSupportedProblem](packers, unpackers)(WarpDescriptor(classOf[OperationNotSupportedProblem].getName), OperationNotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ArgumentProblem](packers, unpackers)(WarpDescriptor(classOf[ArgumentProblem].getName), ArgumentProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[EmptyCollectionProblem](packers, unpackers)(WarpDescriptor(classOf[EmptyCollectionProblem].getName), EmptyCollectionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[InvalidCastProblem](packers, unpackers)(WarpDescriptor(classOf[InvalidCastProblem].getName), InvalidCastProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[PersistenceProblem](packers, unpackers)(WarpDescriptor(classOf[PersistenceProblem].getName), PersistenceProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotSupportedProblem](packers, unpackers)(WarpDescriptor(classOf[NotSupportedProblem].getName), NotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[MappingProblem](packers, unpackers)(WarpDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[MappingProblem](packers, unpackers)(WarpDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[SerializationProblem](packers, unpackers)(WarpDescriptor(classOf[SerializationProblem].getName), SerializationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[StartupProblem](packers, unpackers)(WarpDescriptor(classOf[StartupProblem].getName), StartupProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[IndexOutOfBoundsProblem](packers, unpackers)(WarpDescriptor(classOf[IndexOutOfBoundsProblem].getName), IndexOutOfBoundsProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[KeyNotFoundProblem](packers, unpackers)(WarpDescriptor(classOf[KeyNotFoundProblem].getName), KeyNotFoundProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ConstraintViolatedProblem](packers, unpackers)(WarpDescriptor(classOf[ConstraintViolatedProblem].getName), ConstraintViolatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ParsingProblem](packers, unpackers)(WarpDescriptor(classOf[ParsingProblem].getName), ParsingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[BadDataProblem](packers, unpackers)(WarpDescriptor(classOf[BadDataProblem].getName), BadDataProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[CollisionProblem](packers, unpackers)(WarpDescriptor(classOf[CollisionProblem].getName), CollisionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotAuthorizedProblem](packers, unpackers)(WarpDescriptor(classOf[NotAuthorizedProblem].getName), NotAuthorizedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotAuthenticatedProblem](packers, unpackers)(WarpDescriptor(classOf[NotAuthenticatedProblem].getName), NotAuthenticatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[AlreadyExistsProblem](packers, unpackers)(WarpDescriptor(classOf[AlreadyExistsProblem].getName), AlreadyExistsProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationCancelledProblem](packers, unpackers)(WarpDescriptor(classOf[OperationCancelledProblem].getName), OperationCancelledProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[BusinessRuleViolatedProblem](packers, unpackers)(WarpDescriptor(classOf[BusinessRuleViolatedProblem].getName), BusinessRuleViolatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[LocaleNotSupportedProblem](packers, unpackers)(WarpDescriptor(classOf[LocaleNotSupportedProblem].getName), LocaleNotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NoSuchElementProblem](packers, unpackers)(WarpDescriptor(classOf[NoSuchElementProblem].getName), NoSuchElementProblem.tupled)

    createAndRegisterDefaultPackerAndUnpacker[RiftWarpProblem](packers, unpackers)(WarpDescriptor(classOf[RiftWarpProblem].getName), RiftWarpProblem.tupled)
  }

  def registerAllCommonProblems(packers: WarpPackers, unpackers: WarpUnpackers) {
    createAndRegisterAllDefaultDecomposersAndRecomposers(packers, unpackers)
    packers.addTyped(createAggregateProblemPacker(WarpDescriptor(classOf[AggregateProblem])))
    unpackers.addTyped(createAggregateProblemUnpacker(WarpDescriptor(classOf[AggregateProblem])))
  }
}