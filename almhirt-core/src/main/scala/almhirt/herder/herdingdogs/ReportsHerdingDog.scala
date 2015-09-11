package almhirt.herder.herdingdogs

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessages
import akka.actor.ActorRef
import almhirt.akkax._

object ReportsHerdingDog {

  val actorname = "reports-herdingdog"
}

private[almhirt] class ReportsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with HasAlmhirtContext with AlmActorLogging {
  import HerderMessages.ReportMessages._

  implicit val executor = almhirtContext.futuresContext

  var reporters: Map[ComponentId, almhirt.herder.Reporter] = Map.empty

  def receiveRunning: Receive = {
    case RegisterReporter(ownerId, reporter) ⇒
      if (ownerId == null) {
        logError(s"$ownerId is null! Sender: ${sender()}")
        reportMajorFailure(UnspecifiedProblem(s"$ownerId is null!. Sender: ${sender()}"))
      } else if (reporter == null) {
        logError(s"Reporter for $ownerId is null!")
        reportMajorFailure(UnspecifiedProblem(s"ComponentControl for $ownerId is null!"))
      } else {
        logInfo(s"""Reporter registered for "${ownerId}".""")
        reporters = reporters + (ownerId → reporter)
      }

    case DeregisterReporter(ownerId) ⇒
      logInfo(s"""Reporter deregistered for "${ownerId}".""")
      reporters = reporters - ownerId

    case GetReportFor(componentId) ⇒
      val pinnedSender = sender()

      reporters.get(componentId) match {
        case Some(reporter) ⇒
          reporter.report.mapOrRecoverThenPipeTo(
            map = report ⇒ ReportFor(componentId, report),
            recover = prob ⇒ GetReportForFailed(componentId, prob))(receiver = sender())
        case None ⇒
          sender() ! GetReportForFailed(componentId, NotFoundProblem(s"No reporter for $componentId"))
      }
    
    case GetReporters =>
      sender() ! Reporters(reporters.toList)
  }

  override def receive: Receive = receiveRunning

  override def preStart() {
    super.preStart()
    logInfo("Starting..")
  }

  override def postStop() {
    super.postStop()
    logInfo("Stopped..")
  }

} 