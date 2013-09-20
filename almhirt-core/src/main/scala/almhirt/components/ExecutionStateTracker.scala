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
  final case class ExecutionTrackingExpired(trackId: String) extends ExecutionFinishedResultMessage

  import scalaz.syntax.validation._
  import akka.pattern.ask
  import akka.util.Timeout
  import almhirt.almfuture.all._

  trait SecondLevelStore {
    def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]]
  }

  import scala.concurrent.ExecutionContext

  def secondLevelStore(actor: ActorRef)(implicit executionContext: ExecutionContext): SecondLevelStore = {
    new SecondLevelStore {
      def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]] =
        (actor ? ExecutionStateStore.GetEntry(trackId))(atMost).successfulAlmFuture[ExecutionStateStore.GetEntryResponse].mapV(res =>
          res match {
            case ExecutionStateStore.GetEntryResult(entry) => entry.success
            case ExecutionStateStore.GetEntryFailure(problem) => problem.failure
          })
    }
  }

  import com.typesafe.config.Config
  import almhirt.configuration._
  def props(configSection: Config, theSecondLevelStore: SecondLevelStore, secondLevelStoreMaxAskDur: FiniteDuration, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      theTargetSize <- configSection.v[Int]("target-size")
      theCeanUpThreshold <- configSection.v[Int]("clean-up-threshold")
      theCleanUpInterval <- configSection.v[FiniteDuration]("clean-up-interval")
    } yield {
      theAlmhirt.log.info(s"""ExecutionStateTracker: "target-size" is $theTargetSize""")
      theAlmhirt.log.info(s"""ExecutionStateTracker: "clean-up-threshold" is $theCeanUpThreshold""")
      theAlmhirt.log.info(s"""ExecutionStateTracker: "clean-up-interval" is ${theCleanUpInterval.defaultUnitString}""")
      Props(new almhirt.components.impl.ExecutionTrackerTemplate with ExecutionStateTracker with Actor with ActorLogging {
        override val executionContext = theAlmhirt.futuresExecutor
        override val publishTo = theAlmhirt.messageBus
        override val canCreateUuidsAndDateTimes = theAlmhirt
        override val secondLevelStore = theSecondLevelStore
        override val secondLevelMaxAskDuration = secondLevelStoreMaxAskDur
        override val targetSize = theTargetSize
        override val cleanUpThreshold = theCeanUpThreshold
        override val cleanUpInterval = theCleanUpInterval

        def receive = handleTrackingMessage

        override def preStart() {
          this.context.system.scheduler.scheduleOnce(cleanUpInterval)(requestCleanUp())(theAlmhirt.futuresExecutor)
          requestSubscriptionChecking()
        }

        override def postStop() {
          log.info(s"""During my lifetime $lifetimeExpiredSubscriptions(${(lifetimeExpiredSubscriptions.toDouble / lifetimeTotalSubscriptions.toDouble) * 100.0}%) subscriptions of $lifetimeTotalSubscriptions expired.""")
          log.info(s"""The following number of states were received:\nStarted: $numStartedReceived\nInProcess: $numInProcessReceived\nSuccessful: $numSuccessfulReceived\nFailed: $numFailedReceived""")
        }
      })
    }

  def props(configSection: Config, secondLevelStore: Option[SecondLevelStore], theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      theSndLvlStMaxAskDur <- if (secondLevelStore.isDefined)
        configSection.v[FiniteDuration]("second-level-store-max-ask-duration")
      else
        theAlmhirt.durations.mediumDuration.success
      actualSecondLevelStore <- secondLevelStore.getOrElse(new SecondLevelStore {
        override def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]] =
          AlmFuture.successful(None)
      }).success
      props <- props(configSection, actualSecondLevelStore, theSndLvlStMaxAskDur, theAlmhirt)
    } yield {
      if (secondLevelStore.isDefined) {
        theAlmhirt.log.info(s"""ExecutionStateTracker: Using a second level store with a "second-level-store-max-ask-duration" of $theSndLvlStMaxAskDur.""")
      } else {
        theAlmhirt.log.info(s"""ExecutionStateTracker: No second level store.""")
      }
      props
    }

  def props(configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    props(configSection, None, theAlmhirt)

  def props(configSection: Config, secondLevelStore: SecondLevelStore, theAlmhirt: Almhirt): AlmValidation[Props] =
    props(configSection, Some(secondLevelStore), theAlmhirt)

  def props(configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(
      props(_, None, theAlmhirt))

  def props(configPath: String, secondLevelStore: SecondLevelStore, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(
      props(_, Some(secondLevelStore), theAlmhirt))

  def props(theAlmhirt: Almhirt): AlmValidation[Props] =
    props("almhirt.execution-state-tracker", theAlmhirt)

  def props(secondLevelStore: SecondLevelStore, theAlmhirt: Almhirt): AlmValidation[Props] =
    props("almhirt.execution-state-tracker", secondLevelStore, theAlmhirt)
}

trait ExecutionStateTracker { actor: Actor with ActorLogging =>
  def handleTrackingMessage: Receive
}


