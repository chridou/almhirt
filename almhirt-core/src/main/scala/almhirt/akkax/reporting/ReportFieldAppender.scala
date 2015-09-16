package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.ProblemCause
import almhirt.akkax.ComponentState
import almhirt.akkax.reporting.AST.RReport

trait ReportFieldAppender[T] {
  def append(label: String, value: T, current: ReportFields): ReportFields
}

trait IdentityAppenders {
  implicit val ReportFieldAppenderIdentityInst: ReportFieldAppender[AST.RValue] = new ReportFieldAppender[AST.RValue] {
    def append(label: String, value: AST.RValue, current: ReportFields): ReportFields = current :+ AST.RField(label, value)
  }
}

trait BasicTypeAppenders {
  implicit val ReportFieldAppenderComponentStateInst: ReportFieldAppender[ComponentState] = new ReportFieldAppender[ComponentState] {
    def append(label: String, value: ComponentState, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RComponentState(value))
  }
  implicit val ReportFieldAppenderStringInst: ReportFieldAppender[String] = new ReportFieldAppender[String] {
    def append(label: String, value: String, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RString(value))
  }
  implicit val ReportFieldAppenderIntInst: ReportFieldAppender[Int] = new ReportFieldAppender[Int] {
    def append(label: String, value: Int, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RInteger(value.toLong))
  }
  implicit val ReportFieldAppenderLongInst: ReportFieldAppender[Long] = new ReportFieldAppender[Long] {
    def append(label: String, value: Long, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RInteger(value))
  }
  implicit val ReportFieldAppenderFloatInst: ReportFieldAppender[Float] = new ReportFieldAppender[Float] {
    def append(label: String, value: Float, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RFloat(value.toDouble))
  }
  implicit val ReportFieldAppenderDoubleInst: ReportFieldAppender[Double] = new ReportFieldAppender[Double] {
    def append(label: String, value: Double, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RFloat(value))
  }
  implicit val ReportFieldAppenderBooleanInst: ReportFieldAppender[Boolean] = new ReportFieldAppender[Boolean] {
    def append(label: String, value: Boolean, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RBool(value))
  }
  implicit val ReportFieldAppenderLocalDateTimeInst: ReportFieldAppender[LocalDateTime] = new ReportFieldAppender[LocalDateTime] {
    def append(label: String, value: LocalDateTime, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RLocalDateTime(value))
  }
  implicit val ReportFieldAppenderZonedDateTimeInst: ReportFieldAppender[ZonedDateTime] = new ReportFieldAppender[ZonedDateTime] {
    def append(label: String, value: ZonedDateTime, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RZonedDateTime(value))
  }
  implicit val ReportFieldAppenderDurationInst: ReportFieldAppender[scala.concurrent.duration.FiniteDuration] = new ReportFieldAppender[scala.concurrent.duration.FiniteDuration] {
    def append(label: String, value: scala.concurrent.duration.FiniteDuration, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RDuration(value))
  }
  implicit val ReportFieldAppenderProblemCauseInst: ReportFieldAppender[ProblemCause] = new ReportFieldAppender[ProblemCause] {
    def append(label: String, value: ProblemCause, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RError(value.message))
  }
}

trait OptionAppenders { self: BasicTypeAppenders with IdentityAppenders ⇒
  private def createOptionSomeAppenderWrapperInst[T](implicit basicAppender: ReportFieldAppender[T]): ReportFieldAppender[Option[T]] = new ReportFieldAppender[Option[T]] {
    def append(label: String, value: Option[T], current: ReportFields): ReportFields =
      value match {
        case Some(v) ⇒ basicAppender.append(label, value.get, current)
        case None    ⇒ current :+ AST.RField(label, AST.RNotAvailable)
      }

  }

  implicit val SomeReportFieldAppenderIdentityInst: ReportFieldAppender[Option[AST.RValue]] = createOptionSomeAppenderWrapperInst[AST.RValue]
  implicit val SomeReportFieldAppenderComponentStateInst: ReportFieldAppender[Option[ComponentState]] = createOptionSomeAppenderWrapperInst[ComponentState]
  implicit val SomeReportFieldAppenderStringInst: ReportFieldAppender[Option[String]] = createOptionSomeAppenderWrapperInst[String]
  implicit val SomeReportFieldAppenderIntInst: ReportFieldAppender[Option[Int]] = createOptionSomeAppenderWrapperInst[Int]
  implicit val SomeReportFieldAppenderLongInst: ReportFieldAppender[Option[Long]] = createOptionSomeAppenderWrapperInst[Long]
  implicit val SomeReportFieldAppenderFloatInst: ReportFieldAppender[Option[Float]] = createOptionSomeAppenderWrapperInst[Float]
  implicit val SomeReportFieldAppenderDoubleInst: ReportFieldAppender[Option[Double]] = createOptionSomeAppenderWrapperInst[Double]
  implicit val SomeReportFieldAppenderBooleanInst: ReportFieldAppender[Option[Boolean]] = createOptionSomeAppenderWrapperInst[Boolean]
  implicit val SomeReportFieldAppenderLocalDateTimeInst: ReportFieldAppender[Option[LocalDateTime]] = createOptionSomeAppenderWrapperInst[LocalDateTime]
  implicit val SomeReportFieldAppenderZonedDateTimeInst: ReportFieldAppender[Option[ZonedDateTime]] = createOptionSomeAppenderWrapperInst[ZonedDateTime]
  implicit val SomeReportFieldAppenderDurationInst: ReportFieldAppender[Option[scala.concurrent.duration.FiniteDuration]] = createOptionSomeAppenderWrapperInst[scala.concurrent.duration.FiniteDuration]
  implicit val SomeReportFieldAppenderProblemCauseInst: ReportFieldAppender[Option[ProblemCause]] = createOptionSomeAppenderWrapperInst[ProblemCause]

}