package almhirt.herder.herdingdogs

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.context._
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
      ignoreCircuitProblems <- ctx.config.v[Boolean]("ignore-circuit-problems")
      maxFailuresForSummary <- ctx.config.v[Int]("max-failures-for-summary")
      unwrapFailures <- ctx.config.v[Boolean]("unwrap-failures")
    } yield Props(new FailuresHerdingDog(ignoreCircuitProblems, maxFailuresForSummary, unwrapFailures))
  }

  val actorname = "failures-herdingdog"
}

private[almhirt] class FailuresHerdingDog(ignoreCircuitProblems: Boolean, maxFailuresForSummary: Int, unwrapFailures: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  implicit val executor = almhirtContext.futuresContext

  var collectedFailures: Map[String, FailuresEntry] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.FailureOccured(name, failure, severity, timestamp) =>
      prepareCause(failure).foreach(p => collectedFailures get name match {
        case Some(entry) =>
          collectedFailures + (name -> entry.add(p, severity, timestamp, maxFailuresForSummary))
        case None =>
          collectedFailures + (name -> FailuresEntry().add(p, severity, timestamp, maxFailuresForSummary))
      })
  }

  def prepareCause(cause: ProblemCause): Option[ProblemCause] = {
    val unwrapped = if (unwrapFailures) cause.unwrap() else cause
    unwrapped match {
      case CauseIsProblem(CircuitOpenProblem(_)) if ignoreCircuitProblems => None
      case _ => Some(cause)
    }
  }

  override def receive: Receive = receiveRunning
} 