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
      warningPercentage ← section.v[Double]("warning-percentage")
      criticalPrcentage ← section.v[Double]("critical-percentage")
      historySize ← section.v[Int]("history-size")
    } yield Props(new RuntimeHerdingDog(historySize, runtimePollingInterval, warningPercentage, criticalPrcentage))
  }

  val actorname = "runtime-herdingdog"
  private val componentId = ComponentId(AppName("almhirt"), ComponentName("runtime-watchdog"))
}

private[almhirt] class RuntimeHerdingDog(
    historySize: Int,
    runtimePollingInterval: FiniteDuration,
    warningPercentage: Double,
    criticalPercentage: Double)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor {
  import HerderMessages.InformationMessages._

  val runtime = Runtime.getRuntime()

  val classLoadingMXBean: Option[java.lang.management.ClassLoadingMXBean] =
    try {
      val bean = java.lang.management.ManagementFactory.getClassLoadingMXBean()
      if (bean != null)
        Some(bean)
      else {
        this.informMentionable("Could not load the ClassLoadingMXBean")
        None
      }
    } catch {
      case scala.util.control.NonFatal(x) ⇒
        this.informImportant("Failed to load the classloader ClassLoadingMXBean")
        this.reportMinorFailure(x)
        None
    }

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
    case PollRuntime ⇒
      val newEntry = RuntimeHistoryEntry(runtime)
      val timestamp = almhirtContext.getUtcTimestamp
      almhirtContext.fireNonStreamEvent(RuntimeStateRecorded(newEntry)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), GlobalComponentId(this.componentNameProvider.componentId)))
      history.add(newEntry)
      val memPercentage = newEntry.usedMemoryAbsolute * 100.0
      if (memPercentage >= criticalPercentage)
        this.informVeryImportant(newEntry.niceString)
      else if (memPercentage >= warningPercentage)
        this.informImportant(newEntry.niceString)
      else
        this.informNotWorthMentioning(newEntry.niceString)

      classLoadingMXBean.foreach { bean ⇒
        this.informMentionable(s"""|Currently loaded classes: ${bean.getLoadedClassCount}
                                   |Total classes loaded: ${bean.getTotalLoadedClassCount}
                                   |Total unloaded classes: ${bean.getUnloadedClassCount}""".stripMargin)
      }

      context.system.scheduler.scheduleOnce(runtimePollingInterval, self, PollRuntime)
  }

  override def receive: Receive = receiveRunning

  override def preStart() {
    self ! PollRuntime
  }
} 
