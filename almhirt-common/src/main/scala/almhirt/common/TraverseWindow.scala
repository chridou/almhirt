package almhirt.common

final case class TraverseWindow(skip: TraverseWindow.LowerBound, take: TraverseWindow.Length) {
  def toInts = (skip.toInt, take.toInt)
}

object TraverseWindow {
  val noWindow = TraverseWindow(SkipNone, TakeAll)

  sealed trait LowerBound { def toInt: Int }
  final case class Skip(amount: Int) extends LowerBound { def toInt: Int = amount }
  case object SkipNone extends LowerBound { val toInt = 0 }

  sealed trait Length { def toInt: Int }
  final case class Take(amount: Int) extends Length { def toInt: Int = amount }
  case object TakeAll extends Length { def toInt: Int = Int.MaxValue }

  object skipStart {
    def apply(amount: Int): LowerBoundAnchor = new LowerBoundAnchor { val captured = Skip(amount) }
    def none: LowerBoundAnchor = new LowerBoundAnchor { val captured = SkipNone }
  }

  sealed trait LowerBoundAnchor {
    def captured: LowerBound
    final def take(amount: Int): TraverseWindow = TraverseWindow(captured, Take(amount))
    final def takeAll: TraverseWindow = TraverseWindow(captured, TakeAll)
  }

  implicit class LowerBoundOps(self: LowerBound) {
    def takeAll: TraverseWindow = TraverseWindow(self, TakeAll)
    def take(amount: Int): TraverseWindow = TraverseWindow(self, Take(amount))
  }
}