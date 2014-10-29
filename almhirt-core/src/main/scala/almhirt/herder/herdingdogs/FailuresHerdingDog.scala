package almhirt.herder.herdingdogs

import org.joda.time.LocalDateTime
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.common._
import almhirt.problem.ProblemCause
import almhirt.problem.CauseIsProblem
import almhirt.herder._
import almhirt.akkax.ComponentId
import almhirt.problem.Severity

object FailuresHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.failures"
    for {
      section <- ctx.config.v[Config](configPath)
      ignoreConsecutiveCircuitProblems <- section.v[Boolean]("ignore-consecutive-circuit-problems")
      historySize <- section.v[Int]("history-size")
      unwrapFailures <- section.v[Boolean]("unwrap-failures")
    } yield Props(new FailuresHerdingDog(ignoreConsecutiveCircuitProblems, historySize, unwrapFailures))
  }

  val actorname = "failures-herdingdog"
}

private[almhirt] class FailuresHerdingDog(ignoreConsecutiveCircuitProblems: Boolean, historySize: Int, unwrapFailures: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.FailureMessages._

  implicit val executor = almhirtContext.futuresContext

  implicit object GetSev extends GetsSeverity[FailuresEntry] {
    def get(from: FailuresEntry): Severity = from._2
  }

  val history = new MutableBadThingsHistories[ComponentId, FailuresEntry](historySize)

  def receiveRunning: Receive = {
    case FailureOccured(componentId, failure, severity, timestamp) =>
      val ignore =
        (for {
          badThing <- history.get(componentId)
          first <- badThing.latestOccurence
          ignore <- first._1 match {
            case CauseIsProblem(CircuitOpenProblem(_)) => Some(ignoreConsecutiveCircuitProblems)
            case _ => Some(false)
          }
        } yield ignore).getOrElse(false)
        
      prepareCause(failure, ignore).foreach(p => history.add(componentId, (p, severity, timestamp)))

    case ReportFailures =>
      val entries = history.allReversed.sorted
      sender() ! ReportedFailures(entries)

    case ReportFailuresFor(componentId) =>
      sender() ! ReportedFailuresFor(componentId, history getImmutableReversed componentId)
  }

  def prepareCause(cause: ProblemCause, ignoreCircuitProblems: Boolean): Option[ProblemCause] = {
    val unwrapped = if (unwrapFailures) cause.unwrap() else cause
    unwrapped match {
      case CauseIsProblem(CircuitOpenProblem(_)) if ignoreCircuitProblems => None
      case _ => Some(cause)
    }
  }

  override def receive: Receive = receiveRunning
} 