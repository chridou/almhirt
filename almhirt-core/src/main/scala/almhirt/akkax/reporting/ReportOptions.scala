package almhirt.akkax.reporting

/**
 * Options for status reports
 *
 * IncludeFields has a higher priority than excludeFields and noNoise.
 *
 *
 * @author douven
 */
final case class ReportOptions(noNoise: Boolean, excludeNotAvailable: Boolean, includeFields: Set[String], excludeFields: Set[String])

object ReportOptions {
  private val noNoiseFields = Set(
    "report-created-on-utc",
    "report-created-on",
    "currently-i-am",
    "date-of-birth",
    "date-of-birth-utc",
    "actor-path",
    "age")

  val everything = ReportOptions(false, false, Set.empty, Set.empty)
  val noNoise = ReportOptions(true, false, Set.empty, noNoiseFields)

  def makeWithDefaults(noNoise: Boolean, excludeNotAvailable: Boolean): ReportOptions = {
    val a = if (excludeNotAvailable) everything.setExculdeNotAvailable else everything
    if (noNoise) a.setNoNoiseExcludeDefaults else a
  }

  implicit class ReportOptionsOpts(val self: ReportOptions) extends AnyVal {
    def setExculdeNotAvailable: ReportOptions = ReportOptions(self.noNoise, true, self.includeFields, self.excludeFields)
    def setNoNoise: ReportOptions = ReportOptions(true, self.excludeNotAvailable, self.includeFields, self.excludeFields)
    def setNoNoiseExcludeDefaults: ReportOptions = ReportOptions(true, self.excludeNotAvailable, self.includeFields, self.excludeFields.union(noNoiseFields))

    def isIncluded(fieldName: String): Boolean =
      if (self.includeFields(fieldName))
        true
      else
        !self.excludeFields(fieldName)

    def filter(report: StatusReport): StatusReport =
      StatusReport(report.fields.filter { field ⇒ isIncluded(field.label) && (!self.excludeNotAvailable || field.value != AST.RNotAvailable) }.map {
        case AST.RField(label, subReport: AST.RReport) ⇒ AST.RField(label, filter(subReport))
        case x                                         ⇒ x
      })
  }
}