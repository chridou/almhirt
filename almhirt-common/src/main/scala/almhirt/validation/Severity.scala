package almhirt.validation

sealed trait Severity {
  def and(other: Severity): Severity = 
    (this,other) match {
    case(Critical,_) => Critical
    case(_,Critical) => Critical
    case(Major,_) => Major
    case(_,Major) => Major
    case(Minor,_) => Major
    case(_,Minor) => Minor
    case _ => NoProblem
  }
}
final case object Critical extends Severity 
final case object Major extends Severity 
final case object Minor extends Severity 
final case object NoProblem extends Severity

