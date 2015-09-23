package almhirt.akkax.reporting

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.akkax._

object StatusReportsCollector {
  def apply(context: ActorContext)(implicit executor: ExecutionContext): StatusReportsCollector =
    new StatusReportsCollector(context, 5.seconds)

  def apply(context: ActorContext, defaultQueryDur: FiniteDuration)(implicit executor: ExecutionContext): StatusReportsCollector =
    new StatusReportsCollector(context, defaultQueryDur)

}

final class StatusReportsCollector private (context: ActorContext, defaultQueryDur: FiniteDuration)(implicit executor: ExecutionContext) {
  import Implicits._
  type Query = (StatusReportOptions, FiniteDuration) ⇒ AlmFuture[StatusReport]
  private case class Entry(label: String, query: Query, maxQueryDur: FiniteDuration)

  private var entries = Vector[Entry]()

  def register(toRegister: ToResolve, label: Option[String] = None, maxQueryDur: Option[FiniteDuration] = None): Unit = {
    val (effeciveLabel, query) = toRegister match {
      case NoResolvingRequired(actorRef) ⇒
        (s"${actorRef.path.name}-report",
          (options: StatusReportOptions, maxDur: FiniteDuration) ⇒ (actorRef ? ActorMessages.SendStatusReport(options))(maxDur).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
            case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
            case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
          })
      case ResolvePath(path) ⇒
        (s"${path.name}-report",
          (options: StatusReportOptions, maxDur: FiniteDuration) ⇒ (context.actorSelection(path) ? ActorMessages.SendStatusReport(options))(maxDur).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
            case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
            case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
          })
      case ResolveSelection(selection) ⇒
        (s"${selection.anchorPath.name}-report", (options: StatusReportOptions, maxDur: FiniteDuration) ⇒
          (selection ? ActorMessages.SendStatusReport(options))(maxDur).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
            case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
            case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
          })
    }

    val newEntries = entries.filterNot { _.label == effeciveLabel } :+ Entry(effeciveLabel, query, maxQueryDur = maxQueryDur getOrElse defaultQueryDur)
    entries = newEntries
  }

  def queryReports(maxQueryDurOverride: Option[FiniteDuration] = None)(options: StatusReportOptions): AlmFuture[Vector[ezreps.ast.EzField]] = {
    val futs = entries.map {
      case Entry(label, query, maxDur) ⇒
        val effMaxDur = maxQueryDurOverride getOrElse maxDur
        query(options, effMaxDur).materializedValidation.map { reportV ⇒ ezreps.ast.EzField(label, reportV) }
    }
    AlmFuture.sequence(futs).map(_.toVector)
  }

  def appendToReport(report: StatusReport, maxQueryDurOverride: Option[FiniteDuration] = None)(options: StatusReportOptions): AlmFuture[StatusReport] =
    queryReports(maxQueryDurOverride)(options).map(fields ⇒ report.addMany(fields: _*)).recover { p ⇒ report ~ ("subreports", ezreps.ast.EzError(p.message)) }

  def addAsSubReport(report: StatusReport, subreportLabel: String = "subreports", maxQueryDurOverride: Option[FiniteDuration] = None)(options: StatusReportOptions): AlmFuture[StatusReport] =
    queryReports(maxQueryDurOverride)(options).map(fields ⇒ report.subReport(subreportLabel, fields: _*)).recover { p ⇒ report ~ ("subreports", ezreps.ast.EzError(p.message)) }

}