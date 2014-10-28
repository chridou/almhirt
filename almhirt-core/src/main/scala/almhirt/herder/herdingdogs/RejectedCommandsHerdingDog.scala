package almhirt.herder.herdingdogs

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.herder._
import almhirt.problem.{ Severity }

import akka.actor.ActorRef

object RejectedCommandsHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.rejected-commands"
    for {
      section <- ctx.config.v[Config](configPath)
      historySize <- section.v[Int]("history-size")
      unwrapFailures <- section.v[Boolean]("unwrap-failures")
      downgradeCommandRepresentation <- section.v[Boolean]("downgrade-command-representations")
    } yield Props(new RejectedCommandsHerdingDog(historySize, unwrapFailures, downgradeCommandRepresentation))
  }

  val actorname = "rejected-commands-herdingdog"
}

private[almhirt] class RejectedCommandsHerdingDog(historySize: Int, unwrapFailures: Boolean, downgradeCommandRepresentation: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.CommandMessages._

  implicit val executor = almhirtContext.futuresContext

  implicit object GetSev extends GetsSeverity[RejectedCommandsEntry] {
    def get(from: RejectedCommandsEntry): Severity = from._3
  }

  val history = new MutableBadThingsHistories[ComponentId, RejectedCommandsEntry](historySize)

  def receiveRunning: Receive = {
    case RejectedCommand(componentId, commandRepr, severity, cause, timestamp) =>
      history.add(componentId, (if(downgradeCommandRepresentation) commandRepr.downgradeToIdAndType else commandRepr, if (unwrapFailures) cause.unwrap() else cause, severity, timestamp))

    case ReportRejectedCommands =>
      val missed = history.all.sorted
      sender() ! RejectedCommands(missed)
      
    case ReportRejectedCommandsFor(componentId) =>
      sender() ! ReportedRejectedCommandsFor(componentId, history getImmutableReversed componentId)
      
  }

  override def receive: Receive = receiveRunning
} 