package riftwarp.serialization.common

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.std.kit._

object Problems {
  type DefaultProblemCreator[T <: Problem] = ((String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])) => T

  def createDefaultPacker[T <: Problem](clazz: Class[_]): WarpPacker[T] with RegisterableWarpPacker = {
    new WarpPacker[T] with RegisterableWarpPacker {
      val warpDescriptor = WarpDescriptor(clazz.getSimpleName())
      val alternativeWarpDescriptors = WarpDescriptor(clazz.getName()) :: Nil
      def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        warpDescriptor ~>
          P("message", what.message) ~>
          P("severity", what.severity.toString()) ~>
          P("category", what.category.toString()) ~>
          MLookUpForgiving[String, Any]("args", what.args) ~>
          WithOpt("cause", what.cause, ProblemCausePacker)
    }
  }

  def createDefaultUnpacker[T <: Problem](clazz: Class[_], creator: DefaultProblemCreator[T]): RegisterableWarpUnpacker[T] = {
    new RegisterableWarpUnpacker[T] {
      val warpDescriptor = WarpDescriptor(clazz.getSimpleName())
      val alternativeWarpDescriptors = WarpDescriptor(clazz.getName()) :: Nil
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

  def createAggregateProblemPacker(): WarpPacker[AggregateProblem] with RegisterableWarpPacker = {
    new WarpPacker[AggregateProblem] with RegisterableWarpPacker {
      val warpDescriptor = WarpDescriptor(classOf[AggregateProblem].getSimpleName())
      val alternativeWarpDescriptors = WarpDescriptor(classOf[AggregateProblem].getName()) :: Nil
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

  def createAggregateProblemUnpacker(): RegisterableWarpUnpacker[AggregateProblem] = {
    new RegisterableWarpUnpacker[AggregateProblem] {
      val warpDescriptor = WarpDescriptor(classOf[AggregateProblem].getSimpleName())
      val alternativeWarpDescriptors = WarpDescriptor(classOf[AggregateProblem].getName()) :: Nil
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

  def createDefaultDecomposerAndWarpUnpacker[T <: Problem](creator: DefaultProblemCreator[T])(implicit tag: ClassTag[T]) =
    (createDefaultPacker[T](tag.runtimeClass), createDefaultUnpacker[T](tag.runtimeClass, creator))

  def createAndRegisterDefaultPackerAndUnpacker[T <: Problem : ClassTag](packers: WarpPackers, unpackers: WarpUnpackers)(creator: DefaultProblemCreator[T]) {
    val (packer, unpacker) = createDefaultDecomposerAndWarpUnpacker(creator)
    packers.addTyped[T](packer)
    unpackers.addTyped[T](unpacker)
  }

  def createAndRegisterAllDefaultDecomposersAndWarpUnpackers(packers: WarpPackers, unpackers: WarpUnpackers) {
    createAndRegisterDefaultPackerAndUnpacker[Problem](packers, unpackers)(UnspecifiedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[UnspecifiedProblem](packers, unpackers)(UnspecifiedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ExceptionCaughtProblem](packers, unpackers)(ExceptionCaughtProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[RegistrationProblem](packers, unpackers)(RegistrationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ServiceNotFoundProblem](packers, unpackers)(ServiceNotFoundProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NoConnectionProblem](packers, unpackers)(NoConnectionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationTimedOutProblem](packers, unpackers)(OperationTimedOutProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationAbortedProblem](packers, unpackers)(OperationAbortedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[IllegalOperationProblem](packers, unpackers)(IllegalOperationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationNotSupportedProblem](packers, unpackers)(OperationNotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ArgumentProblem](packers, unpackers)(ArgumentProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[EmptyCollectionProblem](packers, unpackers)(EmptyCollectionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[InvalidCastProblem](packers, unpackers)(InvalidCastProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[PersistenceProblem](packers, unpackers)(PersistenceProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotSupportedProblem](packers, unpackers)(NotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[MappingProblem](packers, unpackers)(MappingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[MappingProblem](packers, unpackers)(MappingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[SerializationProblem](packers, unpackers)(SerializationProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[StartupProblem](packers, unpackers)(StartupProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[IndexOutOfBoundsProblem](packers, unpackers)(IndexOutOfBoundsProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[KeyNotFoundProblem](packers, unpackers)(KeyNotFoundProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ConstraintViolatedProblem](packers, unpackers)(ConstraintViolatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[ParsingProblem](packers, unpackers)(ParsingProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[BadDataProblem](packers, unpackers)(BadDataProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[CollisionProblem](packers, unpackers)(CollisionProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotAuthorizedProblem](packers, unpackers)(NotAuthorizedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NotAuthenticatedProblem](packers, unpackers)(NotAuthenticatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[AlreadyExistsProblem](packers, unpackers)(AlreadyExistsProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[OperationCancelledProblem](packers, unpackers)(OperationCancelledProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[BusinessRuleViolatedProblem](packers, unpackers)(BusinessRuleViolatedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[LocaleNotSupportedProblem](packers, unpackers)(LocaleNotSupportedProblem.tupled)
    createAndRegisterDefaultPackerAndUnpacker[NoSuchElementProblem](packers, unpackers)(NoSuchElementProblem.tupled)

    createAndRegisterDefaultPackerAndUnpacker[RiftWarpProblem](packers, unpackers)(RiftWarpProblem.tupled)
  
    packers.addPredicated(x => x.isInstanceOf[Problem], createDefaultPacker[Problem](classOf[Problem]))
  }

  def registerAllCommonProblems(packers: WarpPackers, unpackers: WarpUnpackers) {
    createAndRegisterAllDefaultDecomposersAndWarpUnpackers(packers, unpackers)
    packers.addTyped(createAggregateProblemPacker())
    unpackers.addTyped(createAggregateProblemUnpacker())
  }
}