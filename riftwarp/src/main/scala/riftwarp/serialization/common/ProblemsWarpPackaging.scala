package riftwarp.serialization.common

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.problem._
import riftwarp._
import riftwarp.std.kit._

object SingleProblemPackaging extends WarpPacker[SingleProblem] with RegisterableWarpPacker with RegisterableWarpUnpacker[SingleProblem] {
  override val warpDescriptor = WarpDescriptor(classOf[SingleProblem].getSimpleName())
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[SingleProblem]) :: WarpDescriptor(classOf[SingleProblem.SingleProblemImpl]) :: Nil
  override def pack(what: SingleProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      P("message", what.message) ~>
      LookUp("problemType", what.problemType).fold(
        fail ⇒ With("problemType", almhirt.problem.problemtypes.UnknownProblem, ProblemTypes.UnknownProblemTypePackaging),
        succ ⇒ succ.success) ~>
        MLookUpForgiving[String, Any]("args", what.args) ~>
        WithOpt("cause", what.cause, ProblemCausePacker)

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[SingleProblem] =
    withFastLookUp(from) { lu ⇒
      for {
        message <- lu.getAs[String]("message")
        args <- lu.getAssocs[String]("args").map(_.toMap)
        problemType <- lu.getTyped[ProblemType]("problemType", None).recover(almhirt.problem.problemtypes.UnknownProblem)
        cause <- lu.tryGetWith("cause", ProblemCauseUnpacker)
      } yield SingleProblem(message, problemType, args, cause)
    }

}

object AggregatedProblemPackaging extends WarpPacker[AggregatedProblem] with RegisterableWarpPacker with RegisterableWarpUnpacker[AggregatedProblem] {
  override val warpDescriptor = WarpDescriptor(classOf[AggregatedProblem].getSimpleName())
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[AggregatedProblem]) :: WarpDescriptor(classOf[AggregatedProblem.AggregateProblemImpl]) :: Nil
  override def pack(what: AggregatedProblem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      MLookUpForgiving[String, Any]("args", what.args) ~>
      CWith("problems", what.problems, ProblemPackaging)

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[AggregatedProblem] =
    withFastLookUp(from) { lu ⇒
      for {
        args <- lu.getAssocs[String]("args").map(_.toMap)
        problems <- lu.getManyWith("problems", ProblemPackaging)
      } yield AggregatedProblem(problems, args)
    }
}

object ProblemPackaging extends WarpPacker[Problem] with RegisterableWarpPacker with RegisterableWarpUnpacker[Problem] with DivertingWarpUnpacker[Problem] with DivertingWarpUnpackerWithAutoRegistration[Problem] {
  override val warpDescriptor = WarpDescriptor(classOf[Problem].getSimpleName())
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Problem]) :: Nil
  override def pack(what: Problem)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case sp: SingleProblem ⇒ SingleProblemPackaging(sp)
      case ap: AggregatedProblem ⇒ AggregatedProblemPackaging(ap)
    }

  override val unpackers = SingleProblemPackaging :: AggregatedProblemPackaging :: Nil
}

object ProblemTypes {
  def createPackaging[T <: almhirt.problem.ProblemType](mainDesc: WarpDescriptor, alternateDescs: List[WarpDescriptor], create: ⇒ T): WarpPacker[T] with RegisterableWarpPacker with RegisterableWarpUnpacker[T] =
    new WarpPacker[T] with RegisterableWarpPacker with RegisterableWarpUnpacker[T] {
      val warpDescriptor = mainDesc
      val alternativeWarpDescriptors = alternateDescs
      def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        WarpObject(Some(this.warpDescriptor), Vector.empty).success
      def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
        create.success
    }

  def createDefaultPackaging[T <: almhirt.problem.ProblemType](create: ⇒ T)(implicit tag: ClassTag[T]): WarpPacker[T] with RegisterableWarpPacker with RegisterableWarpUnpacker[T] =
    createPackaging[T](tag.runtimeClass.getSimpleName().filterNot(_ == '$'), List(tag.runtimeClass.getName.filterNot(_ == '$'), tag.runtimeClass.getName), create)

  val UnknownProblemTypePackaging = createDefaultPackaging[almhirt.problem.problemtypes.UnknownProblem.type](almhirt.problem.problemtypes.UnknownProblem)
  val UnspecifiedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.UnspecifiedProblem.type](almhirt.problem.problemtypes.UnspecifiedProblem)
  val MultipleProblemsPackaging = createDefaultPackaging[almhirt.problem.problemtypes.MultipleProblems.type](almhirt.problem.problemtypes.MultipleProblems)
  val ExceptionCaughtProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ExceptionCaughtProblem.type](almhirt.problem.problemtypes.ExceptionCaughtProblem)
  val RegistrationProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.RegistrationProblem.type](almhirt.problem.problemtypes.RegistrationProblem)
  val ServiceNotFoundProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ServiceNotFoundProblem.type](almhirt.problem.problemtypes.ServiceNotFoundProblem)
  val ServiceNotAvailableProblemProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ServiceNotAvailableProblem.type](almhirt.problem.problemtypes.ServiceNotAvailableProblem)
  val ServiceBusyProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ServiceBusyProblem.type](almhirt.problem.problemtypes.ServiceBusyProblem)
  val ServiceBrokenProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ServiceBrokenProblem.type](almhirt.problem.problemtypes.ServiceBrokenProblem)
  val ServiceShutDownProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ServiceShutDownProblem.type](almhirt.problem.problemtypes.ServiceShutDownProblem)
  val DependencyNotFoundProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.DependencyNotFoundProblem.type](almhirt.problem.problemtypes.DependencyNotFoundProblem)
  val NoConnectionProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.NoConnectionProblem.type](almhirt.problem.problemtypes.NoConnectionProblem)
  val OperationTimedOutProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.OperationTimedOutProblem.type](almhirt.problem.problemtypes.OperationTimedOutProblem)
  val OperationAbortedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.OperationAbortedProblem.type](almhirt.problem.problemtypes.OperationAbortedProblem)
  val IllegalOperationProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.IllegalOperationProblem.type](almhirt.problem.problemtypes.IllegalOperationProblem)
  val OperationNotSupportedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.OperationNotSupportedProblem.type](almhirt.problem.problemtypes.OperationNotSupportedProblem)
  val ArgumentProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ArgumentProblem.type](almhirt.problem.problemtypes.ArgumentProblem)
  val EmptyCollectionProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.EmptyCollectionProblem.type](almhirt.problem.problemtypes.EmptyCollectionProblem)
  val MandatoryDataProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.MandatoryDataProblem.type](almhirt.problem.problemtypes.MandatoryDataProblem)
  val InvalidCastProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.InvalidCastProblem.type](almhirt.problem.problemtypes.InvalidCastProblem)
  val PersistenceProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.PersistenceProblem.type](almhirt.problem.problemtypes.PersistenceProblem)
  val MappingProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.MappingProblem.type](almhirt.problem.problemtypes.MappingProblem)
  val SerializationProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.SerializationProblem.type](almhirt.problem.problemtypes.SerializationProblem)
  val StartupProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.StartupProblem.type](almhirt.problem.problemtypes.StartupProblem)
  val IndexOutOfBoundsProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.IndexOutOfBoundsProblem.type](almhirt.problem.problemtypes.IndexOutOfBoundsProblem)
  val NotFoundProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.NotFoundProblem.type](almhirt.problem.problemtypes.NotFoundProblem)
  val ConstraintViolatedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ConstraintViolatedProblem.type](almhirt.problem.problemtypes.ConstraintViolatedProblem)
  val ParsingProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.ParsingProblem.type](almhirt.problem.problemtypes.ParsingProblem)
  val BadDataProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.BadDataProblem.type](almhirt.problem.problemtypes.BadDataProblem)
  val CollisionProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.CollisionProblem.type](almhirt.problem.problemtypes.CollisionProblem)
  val NotAuthorizedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.NotAuthorizedProblem.type](almhirt.problem.problemtypes.NotAuthorizedProblem)
  val NotAuthenticatedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.NotAuthenticatedProblem.type](almhirt.problem.problemtypes.NotAuthenticatedProblem)
  val AlreadyExistsProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.AlreadyExistsProblem.type](almhirt.problem.problemtypes.AlreadyExistsProblem)
  val OperationCancelledProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.OperationCancelledProblem.type](almhirt.problem.problemtypes.OperationCancelledProblem)
  val BusinessRuleViolatedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.BusinessRuleViolatedProblem.type](almhirt.problem.problemtypes.BusinessRuleViolatedProblem)
  val LocaleNotSupportedProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.LocaleNotSupportedProblem.type](almhirt.problem.problemtypes.LocaleNotSupportedProblem)
  val NoSuchElementProblemPackaging = createDefaultPackaging[almhirt.problem.problemtypes.NoSuchElementProblem.type](almhirt.problem.problemtypes.NoSuchElementProblem)
  val RiftWarpSerializationProblemPackaging = createDefaultPackaging[RiftWarpSerializationProblem.type](RiftWarpSerializationProblem)
  val RiftWarpDeserializationProblemPackaging = createDefaultPackaging[RiftWarpDeserializationProblem.type](RiftWarpDeserializationProblem)

  def registerPackers(to: WarpPackers) {
    to.addTyped(UnknownProblemTypePackaging)
    to.addTyped(UnspecifiedProblemPackaging)
    to.addTyped(MultipleProblemsPackaging)
    to.addTyped(ExceptionCaughtProblemPackaging)
    to.addTyped(RegistrationProblemPackaging)
    to.addTyped(ServiceNotFoundProblemPackaging)
    to.addTyped(ServiceNotAvailableProblemProblemPackaging )
    to.addTyped(ServiceBusyProblemPackaging)
    to.addTyped(ServiceBrokenProblemPackaging)
    to.addTyped(ServiceShutDownProblemPackaging)
    to.addTyped(DependencyNotFoundProblemPackaging)
    to.addTyped(NoConnectionProblemPackaging)
    to.addTyped(OperationTimedOutProblemPackaging)
    to.addTyped(OperationAbortedProblemPackaging)
    to.addTyped(IllegalOperationProblemPackaging)
    to.addTyped(OperationNotSupportedProblemPackaging)
    to.addTyped(ArgumentProblemPackaging)
    to.addTyped(EmptyCollectionProblemPackaging)
    to.addTyped(MandatoryDataProblemPackaging)
    to.addTyped(InvalidCastProblemPackaging)
    to.addTyped(PersistenceProblemPackaging)
    to.addTyped(MappingProblemPackaging)
    to.addTyped(SerializationProblemPackaging)
    to.addTyped(StartupProblemPackaging)
    to.addTyped(IndexOutOfBoundsProblemPackaging)
    to.addTyped(NotFoundProblemPackaging)
    to.addTyped(ConstraintViolatedProblemPackaging)
    to.addTyped(ParsingProblemPackaging)
    to.addTyped(BadDataProblemPackaging)
    to.addTyped(CollisionProblemPackaging)
    to.addTyped(NotAuthorizedProblemPackaging)
    to.addTyped(NotAuthenticatedProblemPackaging)
    to.addTyped(AlreadyExistsProblemPackaging)
    to.addTyped(OperationCancelledProblemPackaging)
    to.addTyped(BusinessRuleViolatedProblemPackaging)
    to.addTyped(LocaleNotSupportedProblemPackaging)
    to.addTyped(NoSuchElementProblemPackaging)
    to.addTyped(RiftWarpSerializationProblemPackaging)
    to.addTyped(RiftWarpDeserializationProblemPackaging)
  }

  def registerUnpackers(to: WarpUnpackers) {
    to.addTyped(UnknownProblemTypePackaging)
    to.addTyped(UnspecifiedProblemPackaging)
    to.addTyped(MultipleProblemsPackaging)
    to.addTyped(ExceptionCaughtProblemPackaging)
    to.addTyped(RegistrationProblemPackaging)
    to.addTyped(ServiceNotFoundProblemPackaging)
    to.addTyped(ServiceNotAvailableProblemProblemPackaging )
     to.addTyped(ServiceBusyProblemPackaging)
   to.addTyped(ServiceBrokenProblemPackaging)
    to.addTyped(ServiceShutDownProblemPackaging)
    to.addTyped(DependencyNotFoundProblemPackaging)
    to.addTyped(NoConnectionProblemPackaging)
    to.addTyped(OperationTimedOutProblemPackaging)
    to.addTyped(OperationAbortedProblemPackaging)
    to.addTyped(IllegalOperationProblemPackaging)
    to.addTyped(OperationNotSupportedProblemPackaging)
    to.addTyped(ArgumentProblemPackaging)
    to.addTyped(EmptyCollectionProblemPackaging)
    to.addTyped(MandatoryDataProblemPackaging)
    to.addTyped(InvalidCastProblemPackaging)
    to.addTyped(PersistenceProblemPackaging)
    to.addTyped(MappingProblemPackaging)
    to.addTyped(SerializationProblemPackaging)
    to.addTyped(StartupProblemPackaging)
    to.addTyped(IndexOutOfBoundsProblemPackaging)
    to.addTyped(NotFoundProblemPackaging)
    to.addTyped(ConstraintViolatedProblemPackaging)
    to.addTyped(ParsingProblemPackaging)
    to.addTyped(BadDataProblemPackaging)
    to.addTyped(CollisionProblemPackaging)
    to.addTyped(NotAuthorizedProblemPackaging)
    to.addTyped(NotAuthenticatedProblemPackaging)
    to.addTyped(AlreadyExistsProblemPackaging)
    to.addTyped(OperationCancelledProblemPackaging)
    to.addTyped(BusinessRuleViolatedProblemPackaging)
    to.addTyped(LocaleNotSupportedProblemPackaging)
    to.addTyped(NoSuchElementProblemPackaging)
    to.addTyped(RiftWarpSerializationProblemPackaging)
    to.addTyped(RiftWarpDeserializationProblemPackaging)
  }

}