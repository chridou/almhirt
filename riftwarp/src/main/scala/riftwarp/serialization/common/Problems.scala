//package riftwarp.serialization.common
//
//import scalaz.std._
//import scalaz.syntax.validation._
//import almhirt.common._
//import riftwarp._
//import riftwarp.inst._
//
//object Problems {
//  def createDefaultDecomposer[T <: Problem](aWarpDescriptor: WarpDescriptor): Decomposer[T] = {
//    new Decomposer[T] {
//      val warpDescriptor = aWarpDescriptor
//      val alternativeWarpDescriptors = Nil
//      def decompose[TDimension <: RiftDimension](what: T, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
//        into.addWarpDescriptor(warpDescriptor)
//          .addString("message", what.message)
//          .addString("severity", what.severity.toString())
//          .addString("category", what.category.toString())
//          .addMapLiberate[String, Any]("args", what.args, None).flatMap(
//            _.addOptionalWith("cause", what.cause, ProblemCauseDecomposer))
//    }
//  }
//
//  def createAggregateProblemDecomposer(aWarpDescriptor: WarpDescriptor): Decomposer[AggregateProblem] = {
//    val inner = createDefaultDecomposer[Problem](aWarpDescriptor)
//    new Decomposer[AggregateProblem] {
//      val warpDescriptor = aWarpDescriptor
//      val alternativeWarpDescriptors = Nil
//      def decompose[TDimension <: RiftDimension](what: AggregateProblem, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
//        for {
//          defaults <- inner.decomposeRaw(what, into)
//          additional <- defaults.addIterableOfComplex("problems", what.problems, None)
//        } yield additional
//    }
//  }
//
//  type DefaultProblemCreator[T <: Problem] = ((String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])) => T
//
//  private def recomposeBaseFields(from: Extractor): AlmValidation[(String, Severity, ProblemCategory, Map[String, Any], Option[ProblemCause])] = {
//    for {
//      message <- from.getString("message")
//      severity <- from.getString("severity").flatMap(Severity.fromString(_))
//      category <- from.getString("category").flatMap(ProblemCategory.fromString(_))
//      args <- from.getMap[String]("args", None)
//      cause <- from.tryGetWith("cause", ProblemCauseRecomposer.recompose)
//    } yield (message, severity, category, args, cause)
//  }
//
//  def createDefaultRecomposer[T <: Problem](aWarpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]): Recomposer[T] = {
//    new Recomposer[T] {
//      val warpDescriptor = aWarpDescriptor
//      val alternativeWarpDescriptors = Nil
//      def recompose(from: Extractor): AlmValidation[T] = {
//        recomposeBaseFields(from).map(x => creator(x).asInstanceOf[T])
//      }
//    }
//  }
//
//  def createAggregateProblemRecomposer(aWarpDescriptor: WarpDescriptor): Recomposer[AggregateProblem] =
//    new Recomposer[AggregateProblem] {
//      val warpDescriptor = aWarpDescriptor
//      val alternativeWarpDescriptors = Nil
//      def recompose(from: Extractor): AlmValidation[AggregateProblem] = {
//        for {
//          baseFields <- recomposeBaseFields(from)
//          problems <- from.getManyComplexByTag[List, Problem]("problems", None)
//        } yield AggregateProblem(baseFields._1, baseFields._2, baseFields._3, baseFields._4, baseFields._5, problems)
//      }
//    }
//
//  def createDefaultDecomposerAndRecomposer[T <: Problem](warpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]) =
//    (createDefaultDecomposer[T](warpDescriptor), createDefaultRecomposer[T](warpDescriptor, creator))
//
//  def createAndRegisterDefaultDecomposerAndRecomposer[T <: Problem](riftwarp: RiftWarp)(warpDescriptor: WarpDescriptor, creator: DefaultProblemCreator[T]) {
//    val (decomposer, recomposer) = createDefaultDecomposerAndRecomposer(warpDescriptor, creator)
//    riftwarp.barracks.addDecomposer(decomposer)
//    riftwarp.barracks.addRecomposer(recomposer)
//  }
//
//  def createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp: RiftWarp) {
//    createAndRegisterDefaultDecomposerAndRecomposer[Problem](riftwarp)(WarpDescriptor(classOf[Problem].getName), UnspecifiedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[UnspecifiedProblem](riftwarp)(WarpDescriptor(classOf[UnspecifiedProblem].getName), UnspecifiedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[ExceptionCaughtProblem](riftwarp)(WarpDescriptor(classOf[ExceptionCaughtProblem].getName), ExceptionCaughtProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[RegistrationProblem](riftwarp)(WarpDescriptor(classOf[RegistrationProblem].getName), RegistrationProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[ServiceNotFoundProblem](riftwarp)(WarpDescriptor(classOf[ServiceNotFoundProblem].getName), ServiceNotFoundProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[NoConnectionProblem](riftwarp)(WarpDescriptor(classOf[NoConnectionProblem].getName), NoConnectionProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[OperationTimedOutProblem](riftwarp)(WarpDescriptor(classOf[OperationTimedOutProblem].getName), OperationTimedOutProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[OperationAbortedProblem](riftwarp)(WarpDescriptor(classOf[OperationAbortedProblem].getName), OperationAbortedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[IllegalOperationProblem](riftwarp)(WarpDescriptor(classOf[IllegalOperationProblem].getName), IllegalOperationProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[OperationNotSupportedProblem](riftwarp)(WarpDescriptor(classOf[OperationNotSupportedProblem].getName), OperationNotSupportedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[ArgumentProblem](riftwarp)(WarpDescriptor(classOf[ArgumentProblem].getName), ArgumentProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[EmptyCollectionProblem](riftwarp)(WarpDescriptor(classOf[EmptyCollectionProblem].getName), EmptyCollectionProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[InvalidCastProblem](riftwarp)(WarpDescriptor(classOf[InvalidCastProblem].getName), InvalidCastProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[PersistenceProblem](riftwarp)(WarpDescriptor(classOf[PersistenceProblem].getName), PersistenceProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[NotSupportedProblem](riftwarp)(WarpDescriptor(classOf[NotSupportedProblem].getName), NotSupportedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[MappingProblem](riftwarp)(WarpDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[MappingProblem](riftwarp)(WarpDescriptor(classOf[MappingProblem].getName), MappingProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[SerializationProblem](riftwarp)(WarpDescriptor(classOf[SerializationProblem].getName), SerializationProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[StartupProblem](riftwarp)(WarpDescriptor(classOf[StartupProblem].getName), StartupProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[IndexOutOfBoundsProblem](riftwarp)(WarpDescriptor(classOf[IndexOutOfBoundsProblem].getName), IndexOutOfBoundsProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[KeyNotFoundProblem](riftwarp)(WarpDescriptor(classOf[KeyNotFoundProblem].getName), KeyNotFoundProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[ConstraintViolatedProblem](riftwarp)(WarpDescriptor(classOf[ConstraintViolatedProblem].getName), ConstraintViolatedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[ParsingProblem](riftwarp)(WarpDescriptor(classOf[ParsingProblem].getName), ParsingProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[BadDataProblem](riftwarp)(WarpDescriptor(classOf[BadDataProblem].getName), BadDataProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[CollisionProblem](riftwarp)(WarpDescriptor(classOf[CollisionProblem].getName), CollisionProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthorizedProblem](riftwarp)(WarpDescriptor(classOf[NotAuthorizedProblem].getName), NotAuthorizedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[NotAuthenticatedProblem](riftwarp)(WarpDescriptor(classOf[NotAuthenticatedProblem].getName), NotAuthenticatedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[AlreadyExistsProblem](riftwarp)(WarpDescriptor(classOf[AlreadyExistsProblem].getName), AlreadyExistsProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[OperationCancelledProblem](riftwarp)(WarpDescriptor(classOf[OperationCancelledProblem].getName), OperationCancelledProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[BusinessRuleViolatedProblem](riftwarp)(WarpDescriptor(classOf[BusinessRuleViolatedProblem].getName), BusinessRuleViolatedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[LocaleNotSupportedProblem](riftwarp)(WarpDescriptor(classOf[LocaleNotSupportedProblem].getName), LocaleNotSupportedProblem.tupled)
//    createAndRegisterDefaultDecomposerAndRecomposer[NoSuchElementProblem](riftwarp)(WarpDescriptor(classOf[NoSuchElementProblem].getName), NoSuchElementProblem.tupled)
//
//    createAndRegisterDefaultDecomposerAndRecomposer[RiftWarpProblem](riftwarp)(WarpDescriptor(classOf[RiftWarpProblem].getName), RiftWarpProblem.tupled)
//  }
//
//  def registerAllCommonProblems(riftwarp: RiftWarp) {
//    createAndRegisterAllDefaultDecomposersAndRecomposers(riftwarp)
//    riftwarp.barracks.addDecomposer(createAggregateProblemDecomposer(WarpDescriptor(classOf[AggregateProblem].getName)))
//    riftwarp.barracks.addRecomposer(createAggregateProblemRecomposer(WarpDescriptor(classOf[AggregateProblem].getName)))
//  }
//}