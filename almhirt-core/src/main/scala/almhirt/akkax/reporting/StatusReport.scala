package almhirt.akkax.reporting

import almhirt.akkax.ComponentState


object StatusReport {
  val empty = AST.RReport(Vector.empty)
  def apply(name: String): AST.RReport = empty.withReportName(name)
  def apply(): AST.RReport = empty

}

