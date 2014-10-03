package almhirt.common

final case class TraverseWindow(skip: TraverseWindow.LowerBound, take: TraverseWindow.Length)

object TraverseWindow {
  val noWindow = TraverseWindow(SkipNone, TakeAll)

  sealed trait LowerBound
  final case class Skip(amount: Int) extends LowerBound
  case object SkipNone extends LowerBound

  sealed trait Length
  final case class Take(amount: Int) extends Length
  case object TakeAll extends Length

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