package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.ProblemCause
import almhirt.akkax.ComponentState

trait ReportFieldAppender[T] {
  def append(label: String, value: T, current: ReportFields): ReportFields
}

trait IdentityAppenders {
  implicit val ReportFieldAppenderReportBasicValueInst: ReportFieldAppender[AST.RBasicValue] = new ReportFieldAppender[AST.RBasicValue] {
    def append(label: String, value: AST.RBasicValue, current: ReportFields): ReportFields = current :+ AST.RField(label, value)
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
  implicit val ReportFieldAppenderProblemCauseInst: ReportFieldAppender[ProblemCause] = new ReportFieldAppender[ProblemCause] {
    def append(label: String, value: ProblemCause, current: ReportFields): ReportFields = current :+ AST.RField(label, AST.RError(value.message))
  }
}

trait OptionAppenders { self: BasicTypeAppenders with IdentityAppenders ⇒
  private def createOptionSomeAppenderWrapperInst[T](implicit basicAppender: ReportFieldAppender[T]): ReportFieldAppender[Some[T]] = new ReportFieldAppender[Some[T]] {
    def append(label: String, value: Some[T], current: ReportFields): ReportFields =
      basicAppender.append(label, value.get, current)
  }

  implicit val SomeReportFieldAppenderComponentStateInst: ReportFieldAppender[Some[ComponentState]] = createOptionSomeAppenderWrapperInst[ComponentState]
  implicit val SomeReportFieldAppenderStringInst: ReportFieldAppender[Some[String]] = createOptionSomeAppenderWrapperInst[String]
  implicit val SomeReportFieldAppenderIntInst: ReportFieldAppender[Some[Int]] = createOptionSomeAppenderWrapperInst[Int]
  implicit val SomeReportFieldAppenderLongInst: ReportFieldAppender[Some[Long]] = createOptionSomeAppenderWrapperInst[Long]
  implicit val SomeReportFieldAppenderFloatInst: ReportFieldAppender[Some[Float]] = createOptionSomeAppenderWrapperInst[Float]
  implicit val SomeReportFieldAppenderDoubleInst: ReportFieldAppender[Some[Double]] = createOptionSomeAppenderWrapperInst[Double]
  implicit val SomeReportFieldAppenderBooleanInst: ReportFieldAppender[Some[Boolean]] = createOptionSomeAppenderWrapperInst[Boolean]
  implicit val SomeReportFieldAppenderLocalDateTimeInst: ReportFieldAppender[Some[LocalDateTime]] = createOptionSomeAppenderWrapperInst[LocalDateTime]
  implicit val SomeReportFieldAppenderZonedDateTimeInst: ReportFieldAppender[Some[ZonedDateTime]] = createOptionSomeAppenderWrapperInst[ZonedDateTime]
  implicit val SomeReportFieldAppenderProblemCauseInst: ReportFieldAppender[Some[ProblemCause]] = createOptionSomeAppenderWrapperInst[ProblemCause]


  implicit val OptionNoneAppenderWrapperInst: ReportFieldAppender[None.type] = new ReportFieldAppender[None.type] {
    def append(label: String, value: None.type, current: ReportFields): ReportFields =
     current :+ AST.RField(label, AST.RNotAvailable)
  }
}