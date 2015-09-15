package almhirt.akkax.reporting

import almhirt.akkax.ComponentState


object StatusReport {
  val empty = AST.RReport(Vector.empty)
  def apply(name: String): AST.RReport = empty.withReportName(name)
  def apply(): AST.RReport = empty

}

object XX {
  import DefaultAppenders._
  
  val x = StatusReport().add("a", "1") + ("b" -> None) + ("c" -> Some("sss"))
}