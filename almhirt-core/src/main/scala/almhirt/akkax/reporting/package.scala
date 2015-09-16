package almhirt.akkax

import scala.language.implicitConversions
import almhirt.common._

package object reporting {
  type StatusReport = AST.RReport

  type ReportFields = Vector[AST.RField]

  object Implicits {
    object DefaultAppenders extends IdentityAppenders with BasicTypeAppenders with OptionAppenders
    object DefaultConverters extends RValueConverters with RValueOptionConverters
    object All extends IdentityAppenders with BasicTypeAppenders with OptionAppenders with RValueConverters with RValueOptionConverters
  }

  implicit def tuple2RField[T](v: (String, T))(implicit converter: RValueConverter[T]): AST.RField = AST.RField(v._1, converter.convert(v._2))
  implicit def almValidation2RValue[T](v: AlmValidation[T])(implicit converter: RValueConverter[T]): AST.RValue =
    v.fold(
      fail ⇒ Implicits.DefaultConverters.RValueConverterProblemCauseInst.convert(fail),
      succ ⇒ converter.convert(succ))
  implicit def tupleAlmValidation2RField[T](v: (String, AlmValidation[T]))(implicit converter: RValueConverter[T]): AST.RField =
    AST.RField(v._1, v._2.fold(
      fail ⇒ Implicits.DefaultConverters.RValueConverterProblemCauseInst.convert(fail),
      succ ⇒ converter.convert(succ)))

  implicit class StatusReportOps(val self: AST.RReport) extends AnyVal {
    def add[T](label: String, value: T)(implicit appender: ReportFieldAppender[T]): AST.RReport =
      AST.RReport(appender.append(label, value, self.fields))

    def +[T](what: (String, T))(implicit appender: ReportFieldAppender[T]): AST.RReport =
      AST.RReport(appender.append(what._1, what._2, self.fields))

    def ++(fields: AST.RField*): AST.RReport =
      AST.RReport(self.fields ++ fields)

    def withReportName(name: String): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("report-name", AST.RString(name)))

    def withComponentState(state: ComponentState): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("created-on", AST.RComponentState(state)))

    def createdNowUtc(implicit ccdt: CanCreateDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("created-on", AST.RLocalDateTime(ccdt.getUtcTimestamp)))

    def createdNow(implicit ccdt: CanCreateDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("component-state", AST.RZonedDateTime(ccdt.getDateTime())))

    def removeNotAvailable: AST.RReport =
      AST.RReport(self.fields.filter {
        case AST.RField(_, AST.RNotAvailable) ⇒ false
        case _                                ⇒ true

      })
  }
}

