package almhirt.environment.configuration

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.ShutDown
import almhirt.environment.configuration.impl.LogBackLoggingAdapter

sealed trait BootstrapperPhaseResult {
  def cleanUps: List[CleanUpAction]
  def andThen(what: => BootstrapperPhaseResult): BootstrapperPhaseResult =
    this match {
      case BootstrapperPhaseSuccess(myCleanUpsSoFar) =>
        try {
          what match {
            case BootstrapperPhaseSuccess(newCleanUps) =>
              BootstrapperPhaseSuccess(newCleanUps ++ myCleanUpsSoFar)
            case BootstrapperPhaseFailure(cause, createdCleanUps) =>
              BootstrapperPhaseFailure(cause, createdCleanUps ++ myCleanUpsSoFar)
          }
        } catch {
          case exn: Exception =>
            val prob = ExceptionCaughtProblem(s"Caught an exception within the bootstrapper: ${exn.getMessage()}", cause = Some(exn))
            BootstrapperPhaseFailure(prob, myCleanUpsSoFar)
        }
      case BootstrapperPhaseFailure(_, _) =>
        this
    }
}

final case class BootstrapperPhaseSuccess(cleanUps: List[CleanUpAction]) extends BootstrapperPhaseResult
final case class BootstrapperPhaseFailure(cause: Problem, cleanUps: List[CleanUpAction]) extends BootstrapperPhaseResult

object BootstrapperPhaseResult {
  implicit class BootstrapperPhaseResultValidationOps(validation: AlmValidation[BootstrapperPhaseResult]) {
    def toBootstrapperPhaseResult(): BootstrapperPhaseResult = {
      validation.fold(
        fail => BootstrapperPhaseFailure(fail, Nil),
        succ => succ)
    }
  }
}

object BootstrapperPhaseSuccess {
  def apply(singleCleanUpAction: CleanUpAction): BootstrapperPhaseSuccess = BootstrapperPhaseSuccess(List(singleCleanUpAction))
  def apply(): BootstrapperPhaseSuccess = BootstrapperPhaseSuccess(Nil)
}

trait PreInitBootstrapperPhase { def preInit(startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }
trait CreatesAlmhirtBootstrapperPhase { def createAlmhirt(startUpLogger: LoggingAdapter): Either[BootstrapperPhaseFailure, (Almhirt, List[CleanUpAction])] = Left(BootstrapperPhaseFailure(NotSupportedProblem("You must create an Almhirt instance. Implement Bootstrapper.createAlmhirt."), Nil)) }
trait CreatesCoreComponentsBootstrapperPhase { def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }
trait CreatesRepositoriesBootstrapperPhase { def createRepositories(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }
trait RegistersCommandHandlersBootstrapperPhase { def registerCommandHandlers(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }
trait PreparesGatewaysBootstrapperPhase { def prepareGateways(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }
trait PostActionsBootstrapperPhase { def postActions(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult = BootstrapperPhaseSuccess(Nil) }

trait Bootstrapper
  extends CreatesAlmhirtBootstrapperPhase
  with PreInitBootstrapperPhase
  with CreatesCoreComponentsBootstrapperPhase
  with CreatesRepositoriesBootstrapperPhase
  with RegistersCommandHandlersBootstrapperPhase
  with PreparesGatewaysBootstrapperPhase
  with PostActionsBootstrapperPhase

/**
 * Mix this one in as the last trait to receive additional logging information about the startup sequence
 *
 * It logs entering and exiting of each phase as a debug message
 */
trait BootstrapperLogEnvelope extends Bootstrapper {
  override def preInit(startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.preInit""")
    val res = super.preInit(startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.preInit""")
    res
  }
  override def createAlmhirt(startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.createAlmhirt""")
    val res = super.createAlmhirt(startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.createAlmhirt""")
    res
  }
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.createCoreComponents""")
    val res = super.createCoreComponents(theAlmhirt, startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.createCoreComponents""")
    res
  }
  override def createRepositories(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.createRepositories""")
    val res = super.createRepositories(theAlmhirt, startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.createRepositories""")
    res
  }
  override def registerCommandHandlers(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.registerCommandHandlers""")
    val res = super.registerCommandHandlers(theAlmhirt, startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.registerCommandHandlers""")
    res
  }
  override def prepareGateways(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.prepareGateways""")
    val res = super.prepareGateways(theAlmhirt, startUpLogger)
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.registerCommandHandlers""")
    res
  }

  override def postActions(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter) = {
    startUpLogger.debug("""I am at the beginning of BootstrapperBase.postActions""")
    val res = super.postActions(theAlmhirt, startUpLogger)
    startUpLogger.debug("""I am about to leave BootstrapperBase.registerCommandHandlers""")
    res
  }
}

object Bootstrapper {
  import scalaz.syntax.validation._
  import almhirt.almvalidation.funs._

  def runBootstrapper(bootstrapper: Bootstrapper)(implicit startUpLogger: LoggingAdapter = LogBackLoggingAdapter()): AlmValidation[(Almhirt, ShutDown)] = {
    var cleanUpsSoFar: List[(String, List[CleanUpAction])] = Nil
    computeSafelyMM {
      startUpLogger.info("Starting bootstrapper phase 1: Pre-init")
      bootstrapper.preInit(startUpLogger) match {
        case BootstrapperPhaseFailure(prob, cleanUps) =>
          startUpLogger.error(s"Bootstrapper phase 1(Pre-init) failed: ${prob.message}")
          CleanUpAction.runCleanUps(("Phase1(Pre-init)", cleanUps) :: cleanUpsSoFar, startUpLogger)
          prob.failure
        case BootstrapperPhaseSuccess(cleanUps) =>
          cleanUpsSoFar = ("Phase1(Pre-init)", cleanUps) :: cleanUpsSoFar
          startUpLogger.info("Finished bootstrapper phase 1: Pre-init")
          startUpLogger.info("Starting bootstrapper phase 2: Create Almhirt")
          bootstrapper.createAlmhirt(startUpLogger) match {
            case Left(BootstrapperPhaseFailure(prob, cleanUps)) =>
              startUpLogger.error(s"Bootstrapper phase 2(Create Almhirt) failed: ${prob.message}")
              CleanUpAction.runCleanUps(("Phase2(Create Almhirt)", cleanUps) :: cleanUpsSoFar, startUpLogger)
              prob.failure
            case Right((theAlmhirt, cleanUps)) =>
              cleanUpsSoFar = ("Phase2(Create Almhirt)", cleanUps) :: cleanUpsSoFar
              startUpLogger.info("Finished bootstrapper phase 2: Create Almhirt")
              startUpLogger.info("Starting bootstrapper phase 3: Create core components")
              bootstrapper.createCoreComponents(theAlmhirt, startUpLogger) match {
                case BootstrapperPhaseFailure(prob, cleanUps) =>
                  startUpLogger.error(s"Bootstrapper phase 3(Create core components) failed: ${prob.message}")
                  CleanUpAction.runCleanUps(("Phase 3(Create core components)", cleanUps) :: cleanUpsSoFar, startUpLogger)
                  prob.failure
                case BootstrapperPhaseSuccess(cleanUps) =>
                  cleanUpsSoFar = ("Phase 3(Create core components)", cleanUps) :: cleanUpsSoFar
                  startUpLogger.info("Finished bootstrapper phase 3: Create core components")
                  startUpLogger.info("Starting bootstrapper phase 4: Create repositories")
                  bootstrapper.createRepositories(theAlmhirt, startUpLogger) match {
                    case BootstrapperPhaseFailure(prob, cleanUps) =>
                      startUpLogger.error(s"Bootstrapper phase 4(Create repositories) failed: ${prob.message}")
                      CleanUpAction.runCleanUps(("Phase 4(Create repositories)", cleanUps) :: cleanUpsSoFar, startUpLogger)
                      prob.failure
                    case BootstrapperPhaseSuccess(cleanUps) =>
                      cleanUpsSoFar = ("Phase 3(Create repositories)", cleanUps) :: cleanUpsSoFar
                      startUpLogger.info("Finished bootstrapper phase 4: Create repositories")
                      startUpLogger.info("Starting bootstrapper phase 5: Register command handlers")
                      bootstrapper.registerCommandHandlers(theAlmhirt, startUpLogger) match {
                        case BootstrapperPhaseFailure(prob, cleanUps) =>
                          startUpLogger.error(s"Bootstrapper phase 5(Register command handlers) failed: ${prob.message}")
                          CleanUpAction.runCleanUps(("Phase 5(Register command handlers)", cleanUps) :: cleanUpsSoFar, startUpLogger)
                          prob.failure
                        case BootstrapperPhaseSuccess(cleanUps) =>
                          cleanUpsSoFar = ("Phase 4(Register command handlers)", cleanUps) :: cleanUpsSoFar
                          startUpLogger.info("Finished bootstrapper phase 5: Register command handlers")
                          startUpLogger.info("Starting bootstrapper phase 6: Prepare gateways")
                          bootstrapper.prepareGateways(theAlmhirt, startUpLogger) match {
                            case BootstrapperPhaseFailure(prob, cleanUps) =>
                              startUpLogger.error(s"Bootstrapper phase 6(Prepare gateways) failed: ${prob.message}")
                              CleanUpAction.runCleanUps(("Phase 6(Prepare gateways)", cleanUps) :: cleanUpsSoFar, startUpLogger)
                              prob.failure
                            case BootstrapperPhaseSuccess(cleanUps) =>
                              cleanUpsSoFar = ("Phase 5(Prepare gateways)", cleanUps) :: cleanUpsSoFar
                              startUpLogger.info("Finished bootstrapper phase 6: Prepare gateways")
                              startUpLogger.info("Starting bootstrapper phase 7: Post actions")
                              bootstrapper.postActions(theAlmhirt, startUpLogger) match {
                                case BootstrapperPhaseFailure(prob, cleanUps) =>
                                  startUpLogger.error(s"Bootstrapper phase 7(Post actions) failed: ${prob.message}")
                                  CleanUpAction.runCleanUps(("Phase 7(Post actions)", cleanUps) :: cleanUpsSoFar, startUpLogger)
                                  prob.failure
                                case BootstrapperPhaseSuccess(cleanUps) =>
                                  cleanUpsSoFar = ("Phase 7(Post actions)", cleanUps) :: cleanUpsSoFar
                                  startUpLogger.info("Finished bootstrapper phase 7: Post actions")
                                  startUpLogger.info("Bootstrapper sequences finished succesfully")
                                  val shutDown = new ShutDown {
                                    def shutDown {
                                      startUpLogger.info("Initialting shut down sequence")
                                      CleanUpAction.runCleanUps(cleanUpsSoFar, startUpLogger)
                                      startUpLogger.info("Shut down sequence finished successfully")
                                    }
                                  }
                                  (theAlmhirt, shutDown).success
                              }
                          }
                      }
                  }
              }
          }
      }
    }(exn => s"The bootstrapper sequence failed with an exception: ${exn.getMessage()}").leftMap(cause =>
      StartupProblem("Could not complete the bootstrapper sequence.", severity = Critical, cause = Some(cause)))
  }
}