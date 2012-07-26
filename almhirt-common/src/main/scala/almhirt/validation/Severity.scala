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
  def level: Int
}
final case object Critical extends Severity {
  val level = 4
}
final case object Major extends Severity  {
  val level = 3
}
final case object Minor extends Severity {
  val level = 2
} 
final case object NoProblem extends Severity {
  val level = 1
}

