package almhirt.components

import scala.concurrent.duration._
import org.joda.time.LocalDateTime
import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.common._

object ExecutionStateTracker {

  sealed trait ExecutionStateTrackerMessage
  final case class GetExecutionStateFor(trackId: String) extends ExecutionStateTrackerMessage
  final case class QueriedExecutionState(trackId: String, executionState: Option[ExecutionState]) extends ExecutionStateTrackerMessage
  final case class SubscribeForFinishedState(trackId: String) extends ExecutionStateTrackerMessage
  final case class UnsubscribeForFinishedState(trackId: String) extends ExecutionStateTrackerMessage

  sealed trait ExecutionFinishedResultMessage extends ExecutionStateTrackerMessage
  final case class FinishedExecutionStateResult(result: ExecutionFinishedState) extends ExecutionFinishedResultMessage
  final case class ExecutionStateTrackingFailed(trackId: String, problem: Problem) extends ExecutionFinishedResultMessage

  import scalaz.syntax.validation._
  import akka.pattern.ask
  import akka.util.Timeout
  import almhirt.almfuture.all._

  import scala.concurrent.ExecutionContext

  import com.typesafe.config.Config
  import almhirt.configuration._
  def props(configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      theTargetSize <- configSection.v[Int]("target-size")
      theCeanUpThreshold <- configSection.v[Int]("clean-up-threshold")
      theCleanUpInterval <- configSection.v[FiniteDuration]("clean-up-interval")
      checkSubscriptions <- configSection.v[Boolean]("check-subscriptions")
      debugMode <- configSection.v[Boolean]("debug")
      subscriptionsOkReportingThres <- configSection.v[Int]("subscriptions-ok-reporting-threshold")
      stateChangedMessageWarningDur <- configSection.v[FiniteDuration]("state-changed-message-warning-age")
      checkDurations <- if (checkSubscriptions) {
        configSection.v[FiniteDuration]("check-subscriptions-interval").flatMap(interval =>
          configSection.v[FiniteDuration]("check-subscriptions-warn-age-lvl1").flatMap(warnAge1 =>
            configSection.v[FiniteDuration]("check-subscriptions-warn-age-lvl2").map(warnAge2 => Some((interval, warnAge1, warnAge2)))))
      } else {
        None.success
      }
      _ <- checkDurations match {
        case Some((_, a, b)) if (a >= b) =>
          ConstraintViolatedProblem(s""""ExecutionStateTracker: check-subscriptions-warn-age-lvl1" must be less than "check-subscriptions-warn-age-lvl2"(${a.defaultUnitString} >= ${b.defaultUnitString}).""").failure
        case _ => ().success
      }
    } yield {
      if (debugMode)
        theAlmhirt.log.info("""ExecutionStateTracker: DEBUG-MODE""")
      theAlmhirt.log.info(s"""ExecutionStateTracker: "target-size" is $theTargetSize""")
      theAlmhirt.log.info(s"""ExecutionStateTracker: "clean-up-threshold" is $theCeanUpThreshold""")
      theAlmhirt.log.info(s"""ExecutionStateTracker: "clean-up-interval" is ${theCleanUpInterval.defaultUnitString}""")
      checkDurations match {
        case Some((interval, t1, t2)) =>
          theAlmhirt.log.info(s"""ExecutionStateTracker: "check-subscriptions" is true""")
          theAlmhirt.log.info(s"""ExecutionStateTracker: "check-subscriptions-interval" is ${interval.defaultUnitString}""")
          theAlmhirt.log.info(s"""ExecutionStateTracker: "check-subscriptions-warn-age-lvl1" is ${t1.defaultUnitString}""")
          theAlmhirt.log.info(s"""ExecutionStateTracker: "check-subscriptions-warn-age-lvl2" is ${t2.defaultUnitString}""")
        case None =>
          theAlmhirt.log.info(s"""ExecutionStateTracker: "check-subscriptions" is false""")
      }
      Props(new almhirt.components.impl.ExecutionTrackerTemplate with ExecutionStateTracker with Actor with ActorLogging {
        override val futuresContext = theAlmhirt.futuresExecutor
        override val numberCruncher = theAlmhirt.numberCruncher
        override val publishTo = theAlmhirt.messageBus
        override val canCreateUuidsAndDateTimes = theAlmhirt
        override val targetSize = theTargetSize
        override val cleanUpThreshold = theCeanUpThreshold
        override val cleanUpInterval = theCleanUpInterval
        override val checkSubscriptions = checkDurations
        override val inDebugMode = debugMode
        override val subscriptionsOkReportingThreshold = subscriptionsOkReportingThres
        override val stateChangedMessageWarningAge = stateChangedMessageWarningDur

        def receive = handleTrackingMessage

        override def preStart() {
          this.context.system.scheduler.scheduleOnce(cleanUpInterval)(requestCleanUp())(theAlmhirt.futuresExecutor)
          requestSubscriptionChecking()
        }

        override def preRestart(reason: Throwable, message: Option[Any]) {
          super.preRestart(reason, message)
          log.warning(s"Execution state tracker restarting: ${reason.getMessage()}")
        }

        override def postStop() {
          log.info(s"""During my lifetime $lifetimeExpiredSubscriptions(${(lifetimeExpiredSubscriptions.toDouble / lifetimeTotalSubscriptions.toDouble) * 100.0}%) subscriptions of $lifetimeTotalSubscriptions expired.""")
          log.info(s"""The following number of states were received:\nStarted: $numStartedReceived\nInProcess: $numInProcessReceived\nSuccessful: $numSuccessfulReceived\nFailed: $numFailedReceived""")
          log.info(s"""I received $numOldMessages execution state(s) older than ${stateChangedMessageWarningAge.defaultUnitString}.""")
        }
      })
    }

  def props(configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(
      props(_, theAlmhirt))

  def props(theAlmhirt: Almhirt): AlmValidation[Props] =
    props("almhirt.execution-state-tracker", theAlmhirt)

  def apply(theAlmhirt: Almhirt, configSection: Config, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] =
    for {
      numActors <- configSection.v[Int]("number-of-actors")
      props <- props(theAlmhirt)
    } yield {
      theAlmhirt.log.info(s"""ExecutionStateTracker: "number-of-actors" is $numActors""")
      if (numActors <= 1) {
        (actorFactory.actorOf(props, "execution-state-tracker"), CloseHandle.noop)
      } else {
        (actorFactory.actorOf(Props(new ExecutionStateTrackerRouter(numActors, props)), "execution-state-tracker"), CloseHandle.noop)
      }
    }

  def apply(theAlmhirt: Almhirt, configPath: String, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      apply(theAlmhirt, configSection, actorFactory: ActorRefFactory))
    
  def apply(theAlmhirt: Almhirt, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] = apply(theAlmhirt, "almhirt.execution-state-tracker", actorFactory)

  def apply(theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] = apply(theAlmhirt, theAlmhirt.actorSystem)

}

trait ExecutionStateTracker { actor: Actor with ActorLogging =>
  def handleTrackingMessage: Receive
}


