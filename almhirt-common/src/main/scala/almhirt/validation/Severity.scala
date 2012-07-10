package almhirt.validation

sealed trait Severity {
  def and(other: Severity): Severity = 
    (this,other) match {
    case(Critical,_) => Critical
    case(_,Critical) => Critical
    case(Major,_) => Major
    case(_,Major) => Major
    case _ => Minor
  }
}
final case object Critical extends Severity 
final case object Major extends Severity 
final case object Minor extends Severity 
