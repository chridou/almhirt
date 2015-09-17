package almhirt.akkax

import scala.language.implicitConversions
import almhirt.common._

package object reporting {
  type StatusReport = AST.RReport

  type ReportFields = Vector[AST.RField]

  type ProblematicOption[T] = AlmValidation[Option[T]]

  object Implicits extends RValueIdentityConverters with RValueConverters with RValueOptionConverters

  def toAST[T](what: T)(implicit converter: RValueConverter[T]): AST.RValue = converter.convert(what)

  implicit def almValidation2RValue[T](v: AlmValidation[T])(implicit converter: RValueConverter[T], pconv: RValueConverter[Problem]): AST.RValue =
    v.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ))

  implicit def almValidationOption2RValue[T](v: ProblematicOption[T])(implicit converter: RValueConverter[Option[T]], pconv: RValueConverter[Problem]): AST.RValue =
    v.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ))

  implicit def toField[T: RValueConverter](v: (String, T)): AST.RField = AST.RField(v._1, toAST(v._2))

  implicit def toFieldFromValidation[T](v: (String, AlmValidation[T]))(implicit converter: RValueConverter[T], pconv: RValueConverter[Problem]): AST.RField =
    AST.RField(v._1, v._2.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ)))

  implicit def toFieldFromProblematicOptionn[T](v: (String, ProblematicOption[T]))(implicit converter: RValueConverter[Option[T]], pconv: RValueConverter[Problem]): AST.RField =
    AST.RField(v._1, v._2.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ)))

  implicit class StatusReportManipulationOps(val self: AST.RReport) extends AnyVal {
    def add(field: AST.RField): AST.RReport =
      AST.RReport(self.fields :+ field)

    def addMany(fields: AST.RField*): AST.RReport =
      self ~~ fields

    def ~(field: AST.RField): AST.RReport =
      AST.RReport(self.fields :+ field)

    def ~~(fields: Iterable[AST.RField]): AST.RReport =
      AST.RReport(self.fields ++ fields)

    def withReportName(name: String): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("report-name", AST.RString(name)))

    def withComponentState(state: ComponentState): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("component-state", AST.RComponentState(state)))

    def createdNowUtc(implicit ccdt: CanCreateDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("report-created-on-utc", AST.RLocalDateTime(ccdt.getUtcTimestamp)))

    def createdNow(implicit ccdt: CanCreateDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("report-created-on", AST.RZonedDateTime(ccdt.getDateTime())))

    def currentlyIAm(doing: String): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("currently-i-am", AST.RString(doing)))

    def born(when: java.time.ZonedDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("date-of-birth", AST.RZonedDateTime(when)))

    def bornUtc(when: java.time.LocalDateTime): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("date-of-birth-utc", AST.RLocalDateTime(when)))

    def age(duration: java.time.Duration): AST.RReport =
      AST.RReport(self.fields :+ AST.RField("age", AST.RJDuration(duration)))

    def removeNotAvailable: AST.RReport =
      AST.RReport(self.fields.filter {
        case AST.RField(_, AST.RNotAvailable) ⇒ false
        case _                                ⇒ true

      })
  }

  implicit class RValueQueryOps(val self: AST.RValue) extends AnyVal {
    def /(label: String): AST.RValue =
      self match {
        case r: AST.RReport ⇒ r.get(label)
        case _              ⇒ AST.RNotAvailable
      }
  }

  implicit class StatusReportQueryOps(val self: AST.RReport) extends AnyVal {
    def contains(label: String): Boolean = get(label) != AST.RNotAvailable

    def get(label: String): AST.RValue = self.fields.find { _.label == label }.map(_.value) getOrElse AST.RNotAvailable

    @scala.annotation.tailrec
    def getByPath(path: List[String]): AST.RValue =
      path match {
        case Nil ⇒
          AST.RNotAvailable
        case label :: Nil ⇒
          get(label)
        case label :: rest ⇒
          get(label) match {
            case r: AST.RReport ⇒
              getByPath(rest)
            case _ ⇒ AST.RNotAvailable
          }
      }
  }

}

