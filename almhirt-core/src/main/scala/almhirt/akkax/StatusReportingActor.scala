package almhirt.akkax

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.herder.HerderMessages.StatusReportMessages
import almhirt.herder.HerderMessages
import almhirt.akkax.reporting.{ StatusReport, ReportOptions }
import almhirt.akkax.reporting.ReportOptions

trait StatusReportingActor { me: AlmActor ⇒

  implicit def almFuture2PipeableFutureReport(future: AlmFuture[StatusReport]): PipeableAlmFutureReport = new PipeableAlmFutureReport(future)
  implicit def future2PipeableFutureReport(future: scala.concurrent.Future[StatusReport])(implicit executor: ExecutionContext): PipeableAlmFutureReport = new PipeableAlmFutureReport(future.toAlmFuture)

  class PipeableAlmFutureReport(future: AlmFuture[StatusReport]) extends AnyRef {
    def pipeReportTo(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext): AlmFuture[StatusReport] = {
      respondForStatusReportResultAsync(future)(receiver)
      future
    }
  }

  def autoAddDateOfBirth: Boolean = true
  def autoAddDateOfBirthUtc: Boolean = false

  final def registerExplicitStatusReporter(reporter: almhirt.herder.StatusReporter)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.RegisterStatusReporter(cnp.componentId, reporter))

  final def registerStatusReporterPF[T: ClassTag](makeRequestReportMsg: ReportOptions ⇒ Any, extractReportPF: PartialFunction[T, AlmValidation[almhirt.akkax.reporting.StatusReport]], description: Option[String] = None)(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val reporter = almhirt.herder.StatusReporter.make(getReport = options ⇒ {
      (self ? makeRequestReportMsg(options))(5.seconds).mapCastTo[T].mapV(extractReportPF)
    }, description)
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def registerStatusReporter(description: Option[String], timeout: FiniteDuration = 5.seconds)(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val _autoAddRunningSince = me.autoAddDateOfBirth
    val _autoAddRunningSinceUtc = me.autoAddDateOfBirthUtc
    val _runningSince = me.born
    val _runningSinceUtc = me.bornUtc
    val ccdt = almhirtContext
    val actorPath = self.path
    val reporter = almhirt.herder.StatusReporter.make(getReport = options ⇒ {
      (self ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
        case ActorMessages.CurrentStatusReport(report) ⇒ {
          val rep1 = if (_autoAddRunningSince) report.born(_runningSince) else report
          val rep2 = if (_autoAddRunningSinceUtc) rep1.bornUtc(_runningSinceUtc) else rep1
          val rep3 = if (report.fields.exists { x ⇒ x.label == "report-created-on" || x.label == "report-created-on-utc" }) rep2 else rep2.createdNow(ccdt)
          val res = if (report.fields.exists { x ⇒ x.label == "age" }) rep3 else rep3.age(java.time.Duration.between(java.time.ZonedDateTime.now(), _runningSince))
          scalaz.Success(res.actorPath(actorPath))
        }
        case ActorMessages.ReportStatusFailed(cause) ⇒ scalaz.Failure(cause.toProblem)
      }
    }, description)
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def deregisterStatusReporter()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.DeregisterStatusReporter(cnp.componentId))

  implicit class StatusReportingActorRecieveOps(val self: Receive) {
    def termininateStatusReporting(onReportRequested: ReportOptions ⇒ AlmValidation[StatusReport]) =
      self orElse ({
        case ActorMessages.SendStatusReport(options) ⇒
          respondForStatusReportResult(onReportRequested(options))(sender())
      }: Receive)

    def termininateStatusReportingF(onReportRequested: ReportOptions ⇒ AlmFuture[StatusReport])(implicit executor: ExecutionContext) =
      self orElse ({
        case ActorMessages.SendStatusReport(options) ⇒
          onReportRequested(options).pipeReportTo(sender())
      }: Receive)
  }

  def sendStatusReport(report: StatusReport)(receiver: ActorRef) {
    receiver ! ActorMessages.CurrentStatusReport(report)
  }

  def respondForStatusReportResult(report: AlmValidation[StatusReport])(receiver: ActorRef) {
    report.fold(
      fail ⇒ receiver ! ActorMessages.ReportStatusFailed(fail),
      succ ⇒ receiver ! ActorMessages.CurrentStatusReport(succ))
  }

  def respondForStatusReportResultAsync(report: AlmFuture[StatusReport])(receiver: ActorRef)(implicit executor: ExecutionContext) {
    report.onComplete(
      fail ⇒ receiver ! ActorMessages.ReportStatusFailed(fail),
      succ ⇒ receiver ! ActorMessages.CurrentStatusReport(succ))
  }

  def reportsStatus(onReportRequested: ReportOptions ⇒ AlmValidation[StatusReport])(receive: Receive) =
    receive.termininateStatusReporting(onReportRequested)

  def reportsStatusF(onReportRequested: ReportOptions ⇒ AlmFuture[StatusReport])(receive: Receive)(implicit executor: ExecutionContext) =
    receive.termininateStatusReportingF(onReportRequested)

  def queryReportFromActor(fromActor: ActorRef, options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (fromActor ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorSelection(fromActorSelection: ActorSelection, options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (fromActorSelection ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromPath(fromPath: ActorPath, options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[StatusReport] =
    (this.context.actorSelection(fromPath) ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorOpt(fromActorOpt: Option[ActorRef], options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromActorOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromActorSelectionOpt(fromActorSelectionOpt: Option[ActorSelection], options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromActorSelectionOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromPathOpt(fromPathOpt: Option[ActorPath], options: ReportOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[StatusReport]] =
    fromPathOpt match {
      case Some(from) ⇒
        (this.context.actorSelection(from) ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

}