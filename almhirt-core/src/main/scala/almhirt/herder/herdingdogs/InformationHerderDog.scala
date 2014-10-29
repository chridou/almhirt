package almhirt.herder.herdingdogs

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.herder._
import almhirt.problem.{ Severity }

import akka.actor.ActorRef

object InformationHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.information"
    for {
      section <- ctx.config.v[Config](configPath)
      historySize <- section.v[Int]("history-size")
    } yield Props(new InformationHerdingDog(historySize))
  }

  val actorname = "information-herdingdog"
}

private[almhirt] class InformationHerdingDog(historySize: Int)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.InformationMessages._

  implicit val executor = almhirtContext.futuresContext

  implicit object GetImp extends GetsImportance[InformationEntry] {
    def get(from: InformationEntry): Importance = from._2
  }

  val history = new MutableImportantThingsHistories[ComponentId, InformationEntry](historySize)

  def receiveRunning: Receive = {
    case Information(componentId, message, importance, timestamp) =>
      history.add(componentId, (message, importance, timestamp))

    case ReportInformation =>
      val ifos = history.allReversed.sorted
      sender() ! ReportedInformation(ifos)
      
    case ReportInformationFor(componentId) =>
      sender() ! ReportedInformationFor(componentId, history getImmutableReversed componentId)
      
  }

  override def receive: Receive = receiveRunning
} 