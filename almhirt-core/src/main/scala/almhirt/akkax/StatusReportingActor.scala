package almhirt.akkax

import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.herder.HerderMessages.StatusReportMessages
import almhirt.herder.HerderMessages
import almhirt.akkax.reporting.StatusReport

trait StatusReportingActor { me: AlmActor ⇒

  def autoAddDateOfBirth: Boolean = true
  def autoAddDateOfBirthUtc: Boolean = false

  final def registerExplicitStatusReporter(reporter: almhirt.herder.StatusReporter)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.RegisterStatusReporter(cnp.componentId, reporter))

  final def registerStatusReporterPF[T: ClassTag](requestReportMsg: Any, extractReportPF: PartialFunction[T, AlmValidation[almhirt.akkax.reporting.StatusReport]])(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val reporter = almhirt.herder.StatusReporter(getReport = () ⇒ {
      (self ? requestReportMsg)(5.seconds).mapCastTo[T].mapV(extractReportPF)
    })
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def registerStatusReporter(timeout: FiniteDuration = 5.seconds)(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val _autoAddRunningSince = me.autoAddDateOfBirth
    val _autoAddRunningSinceUtc = me.autoAddDateOfBirthUtc
    val _runningSince = me.born
    val _runningSinceUtc = me.bornUtc
    val ccdt = almhirtContext
    val reporter = almhirt.herder.StatusReporter(getReport = () ⇒ {
      (self ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
        case ActorMessages.CurrentStatusReport(report) ⇒ {
          val rep1 = if (_autoAddRunningSince) report.born(_runningSince) else report
          val rep2 = if (_autoAddRunningSinceUtc) rep1.bornUtc(_runningSinceUtc) else rep1
          val res = if (report.fields.exists { x ⇒ x.label == "report-created-on" || x.label == "report-created-on-utc" }) rep2 else rep2.createdNow(ccdt)
          scalaz.Success(res)
        }
        case ActorMessages.ReportStatusFailed(cause) ⇒ scalaz.Failure(cause.toProblem)
      }
    })
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def deregisterStatusReporter()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.DeregisterStatusReporter(cnp.componentId))

  implicit class StatusReportingActorRecieveOps(val self: Receive) {
    def termininateStatusReporting(onReportRequested: () ⇒ AlmValidation[StatusReport]) =
      self orElse ({
        case ActorMessages.ReportStatus ⇒
          onReportRequested().fold(
            fail ⇒ sender() ! ActorMessages.ReportStatusFailed(fail),
            succ ⇒ sender ! ActorMessages.CurrentStatusReport(succ))
      }: Receive)

    def termininateStatusReportingF(onReportRequested: () ⇒ AlmFuture[StatusReport])(implicit executor: ExecutionContext) =
      self orElse ({
        case ActorMessages.ReportStatus ⇒
          onReportRequested().mapOrRecoverThenPipeTo(
            map = ActorMessages.CurrentStatusReport(_),
            recover = ActorMessages.ReportStatusFailed(_))(sender())
      }: Receive)
  }

  def reportsStatus(onReportRequested: () ⇒ AlmValidation[StatusReport])(receive: Receive) =
    receive.termininateStatusReporting(onReportRequested)

  def reportsStatusF(onReportRequested: () ⇒ AlmFuture[StatusReport])(receive: Receive)(implicit executor: ExecutionContext) =
    receive.termininateStatusReportingF(onReportRequested)

  def queryReportFromActor(fromActor: ActorRef, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (fromActor ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorSelection(fromActorSelection: ActorSelection, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (fromActorSelection ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromPath(fromPath: ActorPath, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (this.context.actorSelection(fromPath) ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorOpt(fromActorOpt: Option[ActorRef], timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromActorOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromActorSelectionOpt(fromActorSelectionOpt: Option[ActorSelection], timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromActorSelectionOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromPathOpt(fromPathOpt: Option[ActorPath], timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromPathOpt match {
      case Some(from) ⇒
        (this.context.actorSelection(from) ? ActorMessages.ReportStatus)(timeout).mapCastTo[ActorMessages.ReportStatusRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

}