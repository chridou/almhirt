package almhirt.herder.herdingdogs

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.common._
import almhirt.problem.ProblemCause
import almhirt.problem.CauseIsProblem
import almhirt.herder._

object FailuresHerdingDog {
  import com.typesafe.config.Config
  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val configPath = "almhirt.herder.herding-dogs.failures"
    for {
      section <- ctx.config.v[Config](configPath)
      ignoreConsecutiveCircuitProblems <- section.v[Boolean]("ignore-consecutive-circuit-problems")
      maxFailuresForSummary <- section.v[Int]("max-failures-for-summary")
      unwrapFailures <- section.v[Boolean]("unwrap-failures")
    } yield Props(new FailuresHerdingDog(ignoreConsecutiveCircuitProblems, maxFailuresForSummary, unwrapFailures))
  }

  val actorname = "failures-herdingdog"
}

private[almhirt] class FailuresHerdingDog(ignoreConsecutiveCircuitProblems: Boolean, maxFailuresForSummary: Int, unwrapFailures: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.FailureMessages._
  
  implicit val executor = almhirtContext.futuresContext

  var collectedFailures: Map[ComponentId, FailuresEntry] = Map.empty

  def receiveRunning: Receive = {
    case FailureOccured(componentId, failure, severity, timestamp) =>
      val ignore =
        (for {
          entry <- collectedFailures.get(componentId)
          first <- entry.summaryQueue.headOption
          ignore <- first._1 match {
            case CauseIsProblem(CircuitOpenProblem(_)) => Some(ignoreConsecutiveCircuitProblems)
            case _ => Some(false)
          }
        } yield ignore).getOrElse(false)
      prepareCause(failure, ignore).foreach(p => collectedFailures get componentId match {
        case Some(entry) =>
          collectedFailures += (componentId -> entry.add(p, severity, timestamp, maxFailuresForSummary))
        case None =>
          collectedFailures += (componentId -> FailuresEntry().add(p, severity, timestamp, maxFailuresForSummary))
      })

    case ReportFailures =>
      val entries = collectedFailures.toSeq
      sender() ! ReportedFailures(entries)

    case ReportFailuresFor(componentId) =>
      sender() ! ReportedFailuresFor(componentId, collectedFailures get componentId)
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