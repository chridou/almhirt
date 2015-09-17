package almhirt.akkax.reporting

import almhirt.akkax.ComponentState

object StatusReport {
  val empty = AST.RReport(Vector.empty)
  def apply(name: String): AST.RReport = empty.withReportName(name)
  def apply(fields: ReportFields): AST.RReport = AST.RReport(fields)
  def apply(name: String, fields: ReportFields): AST.RReport = StatusReport(name) ~~ fields
  def apply(): AST.RReport = empty

}

