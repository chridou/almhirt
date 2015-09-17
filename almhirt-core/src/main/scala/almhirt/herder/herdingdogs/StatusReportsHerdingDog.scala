package almhirt.herder.herdingdogs

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessages
import akka.actor.ActorRef
import almhirt.akkax._

object StatusReportsHerdingDog {

  val actorname = "status-reports-herdingdog"
}

private[almhirt] class StatusReportsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with HasAlmhirtContext with AlmActorLogging {
  import HerderMessages.StatusReportMessages._

  implicit val executor = almhirtContext.futuresContext

  var reporters: Map[ComponentId, almhirt.herder.StatusReporter] = Map.empty

  def receiveRunning: Receive = {
    case RegisterStatusReporter(ownerId, reporter) ⇒
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

    case DeregisterStatusReporter(ownerId) ⇒
      logInfo(s"""Reporter deregistered for "${ownerId}".""")
      reporters = reporters - ownerId

    case GetStatusReportFor(componentId, options) ⇒
      val pinnedSender = sender()

      reporters.get(componentId) match {
        case Some(reporter) ⇒
          reporter.report(options).mapOrRecoverThenPipeTo(
            map = report ⇒ StatusReportFor(componentId, report),
            recover = prob ⇒ GetStatusReportForFailed(componentId, prob))(receiver = sender())
        case None ⇒
          sender() ! GetStatusReportForFailed(componentId, NotFoundProblem(s"No reporter for $componentId"))
      }
    
    case GetStatusReporters =>
      sender() ! StatusReporters(reporters.toList.sortBy(_._1))
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