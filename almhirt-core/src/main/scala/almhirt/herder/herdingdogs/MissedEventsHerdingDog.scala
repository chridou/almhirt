package almhirt.herder.herdingdogs

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.herder._
import almhirt.problem.{ Severity }

import akka.actor.ActorRef

object MissedEventsHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.missed-events"
    for {
      section <- ctx.config.v[Config](configPath)
      historySize <- section.v[Int]("history-size")
      unwrapFailures <- section.v[Boolean]("unwrap-failures")
    } yield Props(new MissedEventsHerdingDog(historySize, unwrapFailures))
  }

  val actorname = "missed-events-herdingdog"
}

private[almhirt] class MissedEventsHerdingDog(historySize: Int, unwrapFailures: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.EventMessages._

  implicit val executor = almhirtContext.futuresContext

  implicit object GetSev extends GetsSeverity[MissedEventsEntry] {
    def get(from: MissedEventsEntry): Severity = from._3
  }

  val history = new MutableBadThingsHistories[ComponentId, MissedEventsEntry](historySize)

  def receiveRunning: Receive = {
    case MissedEvent(componentId, event, severity, cause, timestamp) =>
      history.add(componentId, (event, if (unwrapFailures) cause.unwrap() else cause, severity, timestamp))

    case ReportMissedEvents =>
      val missed = history.all.sorted
      sender() ! MissedEvents(missed)
      
    case ReportMissedEventsFor(componentId) =>
      sender() ! ReportedMissedEventsFor(componentId, history getImmutable componentId)
      
  }

  override def receive: Receive = receiveRunning
} 