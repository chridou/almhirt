package almhirt.problem

object problemtypes {
  case object UnknownProblem extends ProblemType

  case object UnspecifiedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, UnspecifiedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, UnspecifiedProblem)
  }

  case object MultipleProblems extends ProblemType {
    def apply(problems: Seq[Problem], args: Map[String, Any] = Map.empty): AggregateProblem =
      AggregateProblem(problems, args)
    def unapply(problem: AggregateProblem): Option[AggregateProblem] = Some(problem)
  }

  case object ExceptionCaughtProblem extends ProblemType {
    def apply(exn: Throwable): SingleProblem =
      SingleProblem(s"""An exception has been caught: "${exn.getMessage()}"""", ExceptionCaughtProblem, Map.empty, cause = Some(exn))
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, ExceptionCaughtProblem)
  }

  /**
   * Should be used in case an attempt to register something somewhere failed.
   */
  case object RegistrationProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, RegistrationProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, RegistrationProblem)
  }

  /**
   * A service couldn't be found.
   */
  case object ServiceNotFoundProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, ServiceNotFoundProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, ServiceNotFoundProblem)
  }

  /**
   * A connection couldn't be established. Use for networking problems.
   */
  case object NoConnectionProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, NoConnectionProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, NoConnectionProblem)
  }

  /**
   * An arbitrary operation timed out.
   * Especially useful in conjunction with futures.
   */
  case object OperationTimedOutProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, OperationTimedOutProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, OperationTimedOutProblem)
  }

  /**
   * An arbitrary operation has been aborted by the system.
   * Not intended to be used in case a user pressed the cancel button.
   * For cancelled operations use [almhirt.OperationCancelledProblem]
   */
  case object OperationAbortedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, OperationAbortedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, OperationAbortedProblem)
  }

  /**
   * The operation was not allowed in the current state/context
   */
  case object IllegalOperationProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, IllegalOperationProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, IllegalOperationProblem)
  }

  /**
   * The operation is simply not supported
   */
  case object OperationNotSupportedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, OperationNotSupportedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, OperationNotSupportedProblem)
  }

  /**
   * An argument violating the operations contract has been passed
   */
  case object ArgumentProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, ArgumentProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, ArgumentProblem)
  }

  /**
   * A collection is empty but at least one element was required
   */
  case object EmptyCollectionProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, EmptyCollectionProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, EmptyCollectionProblem)
  }

  /**
   * A required value was empty
   */
  case object MandatoryDataProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, MandatoryDataProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, MandatoryDataProblem)
  }

  /**
   * As instanceOf "failed"
   */
  case object InvalidCastProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, InvalidCastProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, InvalidCastProblem)
  }

  /**
   * There is a problem with the persistent store.
   */
  case object PersistenceProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, PersistenceProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, PersistenceProblem)
  }

  /**
   * Some data structure couldn't be mapped from one to another.
   */
  case object MappingProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, MappingProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, MappingProblem)
  }

  /**
   * Some data couldn't be (de-)serialized
   */
  case object SerializationProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, SerializationProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, SerializationProblem)
  }

  /**
   * The application couldn't be started properly
   */
  case object StartupProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, StartupProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, StartupProblem)
  }

  case object IndexOutOfBoundsProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, IndexOutOfBoundsProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, IndexOutOfBoundsProblem)
  }

  /**
   * Data couldn't be found. Use when looking for an entity or something similar. Do not use for a missing key in a map or so.
   */
  case object NotFoundProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, NotFoundProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, NotFoundProblem)
  }

  /**
   * A constraint on an operation has been violated (usually)by a user(or a client).
   */
  case object ConstraintViolatedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, ConstraintViolatedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, ConstraintViolatedProblem)
  }

  /**
   * A String couldn't be parsed. Usually used for failures when parsing DSLs
   */
  case object ParsingProblem extends ProblemType {
    def apply(msg: String, badInput: Option[String] = None, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem = {
      val completeArgs = badInput.map(bi ⇒ args + ("bad_input" -> badInput)).getOrElse(args)
      SingleProblem(msg, ParsingProblem, completeArgs, cause)
    }
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, ParsingProblem)
  }

  /**
   * Some data is invalid. The key gives the context
   */
  case object BadDataProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, BadDataProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, BadDataProblem)
  }

  /**
   * Something has been changed by someone else. Stale data etc..
   */
  case object CollisionProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, CollisionProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, CollisionProblem)
  }

  case object NotAuthorizedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, NotAuthorizedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, NotAuthorizedProblem)
  }

  case object NotAuthenticatedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, NotAuthenticatedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, NotAuthenticatedProblem)
  }

  /**
   * Something has already been created. Don't try again...
   */
  case object AlreadyExistsProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, AlreadyExistsProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, AlreadyExistsProblem)
  }

  /**
   * Some external stimulus has cancelled an operation
   */
  case object OperationCancelledProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, OperationCancelledProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, OperationCancelledProblem)
  }

  /**
   * A rule on a business process has been violated
   */
  case object BusinessRuleViolatedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, BusinessRuleViolatedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, BusinessRuleViolatedProblem)
  }

  /**
   * This locale simply isn't supported.
   */
  case object LocaleNotSupportedProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, LocaleNotSupportedProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, LocaleNotSupportedProblem)
  }

  /**
   * An expected element was not present in some kind of collection
   */
  case object NoSuchElementProblem extends ProblemType {
    def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
      SingleProblem(msg, NoSuchElementProblem, args, cause)
    def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, NoSuchElementProblem)
  }
}