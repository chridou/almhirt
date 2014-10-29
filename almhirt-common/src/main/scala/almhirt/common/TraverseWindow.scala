package almhirt.common

import scalaz.Validation.FlatMap._

final case class TraverseWindow(skip: TraverseWindow.LowerBound, take: TraverseWindow.Length) {
  def toInts = (skip.toInt, take.toInt)
}

object TraverseWindow {
  val noWindow = TraverseWindow(SkipNone, TakeAll)

  def fromIntOptions(skip: Option[Int], take: Option[Int]): TraverseWindow = {
    val s = skip match {
      case None ⇒ SkipNone
      case Some(x) if x <= 0 ⇒ SkipNone
      case Some(x) ⇒ Skip(x)
    }
    val t = take match {
      case None ⇒ TakeAll
      case Some(x) if x <= 0 ⇒ Take(0)
      case Some(x) ⇒ Take(x)
    }

    TraverseWindow(s, t)
  }

  def parseFromStringOptions(skip: Option[String], take: Option[String]): AlmValidation[TraverseWindow] = {
    import almhirt.almvalidation.kit._
    val trimmedSkip = skip.flatMap(str ⇒ if (str.trim().isEmpty()) None else Some(str))
    val trimmedTake = take.flatMap(str ⇒ if (str.trim().isEmpty()) None else Some(str))
    for {
      sOpt ← trimmedSkip.map(_.toIntAlm).validationOut
      tOpt ← trimmedTake.map(_.toIntAlm).validationOut
    } yield fromIntOptions(sOpt, tOpt)
  }

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