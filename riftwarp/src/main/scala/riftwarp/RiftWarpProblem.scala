package riftwarp

import almhirt.common._
import almhirt.problem._

trait RiftWarpProblem extends ProblemType

object RiftWarpProblem {
  def unapply(problem: SingleProblem): Option[RiftWarpProblem] =
    problem.problemType match {
      case x: RiftWarpSerializationProblem.type => Some(x)
      case x: RiftWarpDeserializationProblem.type => Some(x)
      case _ => None
    }
}

case object RiftWarpSerializationProblem extends RiftWarpProblem {
  def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
    SingleProblem(msg, RiftWarpSerializationProblem, args, cause)
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, RiftWarpSerializationProblem)
  
}

case object RiftWarpDeserializationProblem extends RiftWarpProblem {
  def apply(msg: String, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
    SingleProblem(msg, RiftWarpDeserializationProblem, args, cause)
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, RiftWarpDeserializationProblem)
}