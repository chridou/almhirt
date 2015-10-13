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
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import ezreps.{ EzReport, EzOptions }
import ezreps.ast._

trait StatusReportingActor { me: AlmActor ⇒

  implicit def almFuture2PipeableFutureReport(future: AlmFuture[EzReport]): PipeableAlmFutureReport = new PipeableAlmFutureReport(future)
  implicit def future2PipeableFutureReport(future: scala.concurrent.Future[EzReport])(implicit executor: ExecutionContext): PipeableAlmFutureReport = new PipeableAlmFutureReport(future.toAlmFuture)

  class PipeableAlmFutureReport(future: AlmFuture[EzReport]) extends AnyRef {
    def pipeReportTo(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext): AlmFuture[EzReport] = {
      respondForStatusReportResultAsync(future)(receiver)
      future
    }
  }

  def autoAddDateOfBirth: Boolean = true
  def autoAddDateOfBirthUtc: Boolean = false

  def statusReportsCollector: Option[StatusReportsCollector] = None

  final def registerExplicitStatusReporter(reporter: almhirt.herder.StatusReporter)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.RegisterStatusReporter(cnp.componentId, reporter))

  final def registerStatusReporterPF[T: ClassTag](makeRequestReportMsg: EzOptions ⇒ Any, extractReportPF: PartialFunction[T, AlmValidation[EzReport]], description: Option[String] = None)(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val reporter = almhirt.herder.StatusReporter.make(getReport = options ⇒ {
      (self ? makeRequestReportMsg(options))(5.seconds).mapCastTo[T].mapV(extractReportPF)
    }, description)
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def registerStatusReporter(description: Option[String], timeout: FiniteDuration = 5.seconds)(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val reporter = almhirt.herder.StatusReporter.make(getReport = options ⇒ {
      (self ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
        case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
        case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
      }
    }, description)
    this.registerExplicitStatusReporter(reporter)(cnp)
  }

  final def deregisterStatusReporter()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.DeregisterStatusReporter(cnp.componentId))

  implicit class StatusReportingActorRecieveOps(val self: Receive) {
    def termininateStatusReporting(onReportRequested: EzOptions ⇒ AlmValidation[EzReport]) =
      self orElse ({
        case ActorMessages.SendStatusReport(options) ⇒
          respondForStatusReportResult(onReportRequested(options))(sender())
        case ActorMessages.ConsiderMeForReporting ⇒
          statusReportsCollector.foreach { _.register(NoResolvingRequired(sender())) }
      }: Receive)

    def termininateStatusReportingF(onReportRequested: EzOptions ⇒ AlmFuture[EzReport])(implicit executor: ExecutionContext) =
      self orElse ({
        case ActorMessages.SendStatusReport(options) ⇒
          onReportRequested(options).pipeReportTo(sender())
        case ActorMessages.ConsiderMeForReporting ⇒
          statusReportsCollector.foreach { _.register(NoResolvingRequired(sender())) }
      }: Receive)

    def terminateRegisterForCollector = self orElse ({
      case ActorMessages.ConsiderMeForReporting ⇒
        statusReportsCollector.foreach { _.register(NoResolvingRequired(sender())) }
    }: Receive)
  }

  def enrichReport(report: EzReport): EzReport = {
    val rep1 = if (me.autoAddDateOfBirth) report.born(me.born) else report
    val rep2 = if (me.autoAddDateOfBirthUtc) rep1.bornUtc(me.bornUtc) else rep1
    val rep3 = if (report.fields.exists { x ⇒ x.label == "report-created-on" || x.label == "report-created-on-utc" }) rep2 else rep2.createdNow(this.almhirtContext)
    val rep4 = if (report.fields.exists { x ⇒ x.label == "age" }) rep3 else rep3.age(java.time.Duration.between(this.almhirtContext.getUtcTimestamp, me.bornUtc))
    val rep5 =
      this match {
        case ca: ControllableActor ⇒
          if (report.fields.exists { x ⇒ x.label == "component-state" }) rep4
          else rep4.withComponentState(ca.componentState)
        case _ ⇒
          rep4
      }
    val rep6 =
      this match {
        case ca: ControllableActor ⇒
          val pauseTokens = ca.pauseTokens
          if(pauseTokens.nonEmpty)
            rep5 ~ ("pause-tokens" -> pauseTokens.mkString(", "))
          else rep5
        case _ ⇒
          rep5
      }

    val res = if (report.fields.exists { x ⇒ x.label == "actor-path" }) rep6 else rep6.actorPath(self.path)
    res
  }

  def sendStatusReport(report: EzReport)(receiver: ActorRef) {
    receiver ! ActorMessages.CurrentStatusReport(enrichReport(report))
  }

  def respondForStatusReportResult(report: AlmValidation[EzReport])(receiver: ActorRef) {
    report.fold(
      fail ⇒ receiver ! ActorMessages.ReportStatusFailed(fail),
      reportSucc ⇒ sendStatusReport(reportSucc)(receiver))
  }

  def respondForStatusReportResultAsync(report: AlmFuture[EzReport])(receiver: ActorRef)(implicit executor: ExecutionContext): Unit = {
    val _autoAddRunningSince = me.autoAddDateOfBirth
    val _autoAddRunningSinceUtc = me.autoAddDateOfBirthUtc
    val _runningSince = me.born
    val _runningSinceUtc = me.bornUtc
    val ccdt = almhirtContext
    val actorPath = self.path

    report.onComplete(
      fail ⇒ receiver ! ActorMessages.ReportStatusFailed(fail),
      reportSucc ⇒ {
        val rep1 = if (_autoAddRunningSince) reportSucc.born(_runningSince) else reportSucc
        val rep2 = if (_autoAddRunningSinceUtc) rep1.bornUtc(_runningSinceUtc) else rep1
        val rep3 = if (reportSucc.fields.exists { x ⇒ x.label == "report-created-on" || x.label == "report-created-on-utc" }) rep2 else rep2.createdNow(this.almhirtContext)
        val rep4 = if (reportSucc.fields.exists { x ⇒ x.label == "age" }) rep3 else rep3.age(java.time.Duration.between(me.bornUtc, this.almhirtContext.getUtcTimestamp))
        val res = if (reportSucc.fields.exists { x ⇒ x.label == "actor-path" }) rep4 else rep4.actorPath(self.path)
        receiver ! ActorMessages.CurrentStatusReport(res)
      })
  }

  def reportsStatus(onReportRequested: EzOptions ⇒ AlmValidation[EzReport])(receive: Receive) =
    receive.termininateStatusReporting(onReportRequested)

  def reportsStatusF(onReportRequested: EzOptions ⇒ AlmFuture[EzReport])(receive: Receive)(implicit executor: ExecutionContext) =
    receive.termininateStatusReportingF(onReportRequested)

  def queryReportFromActor(fromActor: ActorRef, options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[EzReport] =
    (fromActor ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorSelection(fromActorSelection: ActorSelection, options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[EzReport] =
    (fromActorSelection ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromPath(fromPath: ActorPath, options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[EzReport] =
    (this.context.actorSelection(fromPath) ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
      case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(report)
      case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
    }

  def queryReportFromActorOpt(fromActorOpt: Option[ActorRef], options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[EzReport]] =
    fromActorOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromActorSelectionOpt(fromActorSelectionOpt: Option[ActorSelection], options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[EzReport]] =
    fromActorSelectionOpt match {
      case Some(from) ⇒
        (from ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def queryReportFromPathOpt(fromPathOpt: Option[ActorPath], options: EzOptions, timeout: FiniteDuration = 5.seconds)(implicit executor: ExecutionContext): AlmFuture[Option[EzReport]] =
    fromPathOpt match {
      case Some(from) ⇒
        (this.context.actorSelection(from) ? ActorMessages.SendStatusReport(options))(timeout).mapCastTo[ActorMessages.SendStatusReportRsp].mapV {
          case ActorMessages.CurrentStatusReport(report) ⇒ scalaz.Success(Some(report))
          case ActorMessages.ReportStatusFailed(cause)   ⇒ scalaz.Failure(cause.toProblem)
        }
      case None ⇒
        AlmFuture.successful(None)
    }

  def appendToReportFromCollector(report: StatusReport, maxQueryDurOverride: Option[FiniteDuration] = None)(options: StatusReportOptions): AlmFuture[StatusReport] =
    statusReportsCollector match {
      case Some(collector) ⇒ collector.appendToReport(report, maxQueryDurOverride)(options)
      case None            ⇒ AlmFuture.successful(report ~ ezreps.ast.EzField("collector-warning", ezreps.ast.EzString("There was no collector to collect status reports")))
    }

  def addAsSubReportFromCollector(report: StatusReport, subreportLabel: String = "subreports", maxQueryDurOverride: Option[FiniteDuration] = None)(options: StatusReportOptions): AlmFuture[StatusReport] =
    statusReportsCollector match {
      case Some(collector) ⇒ collector.addAsSubReport(report, subreportLabel, maxQueryDurOverride)(options)
      case None            ⇒ AlmFuture.successful(report ~ ezreps.ast.EzField("collector-warning", ezreps.ast.EzString("There was no collector to collect status reports")))
    }

}