package almhirt.herder.herdingdogs

import scala.concurrent.duration.FiniteDuration
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.akkax._
import almhirt.herder._
import akka.actor.ActorRef

object RuntimeHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.runtime"
    for {
      section ← ctx.config.v[Config](configPath)
      runtimePollingInterval ← section.v[FiniteDuration]("polling-interval")
      historySize ← section.v[Int]("history-size")
    } yield Props(new RuntimeHerdingDog(historySize, runtimePollingInterval))
  }

  val actorname = "runtime-herdingdog"
  private val componentId = ComponentId(AppName("almhirt"), ComponentName("runtime-watchdog"))
}

private[almhirt] class RuntimeHerdingDog(historySize: Int, runtimePollingInterval: FiniteDuration)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor {
  import HerderMessages.InformationMessages._

  val runtime = Runtime.getRuntime()

  implicit override val componentNameProvider = new ActorComponentIdProvider {
    val componentId = RuntimeHerdingDog.componentId
  }

  implicit val executor = almhirtContext.futuresContext

  implicit object GetImp extends GetsImportance[InformationEntry] {
    def get(from: InformationEntry): Importance = from._2
  }

  val history = new MutableRuntimeHistory(historySize)

  private object PollRuntime

  def receiveRunning: Receive = {
    case PollRuntime =>
      val newEntry = RuntimeHistoryEntry(runtime)
      history.add(newEntry)
      this.informNotWorthMentioning(newEntry.niceString)
      context.system.scheduler.scheduleOnce(runtimePollingInterval, self, PollRuntime)
  }

  override def receive: Receive = receiveRunning

  override def preStart() {
    self ! PollRuntime
  }
} 
