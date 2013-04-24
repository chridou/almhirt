package riftwarp.warpsequence

sealed trait WarpAction {
  def actionCode: Int
}

object StringWarpAction extends WarpAction { override val actionCode = 1 }
object OptionalStringWarpAction extends WarpAction { override val actionCode = -1 }

object ItemsWarpAction extends WarpAction { override val actionCode = 100 }
object OptionalItemsWarpAction extends WarpAction { override val actionCode = -100 }

object MapWarpAction extends WarpAction { override val actionCode = 200 }
object OptionalMapWarpAction extends WarpAction { override val actionCode = -200 }

object TreeWarpAction extends WarpAction { override val actionCode = 300 }
object OptionalTreeWarbAction extends WarpAction { override val actionCode = -300 }

object ComplexWarpAction extends WarpAction { override val actionCode = 400 }
object OptionalComplexWarpAction extends WarpAction { override val actionCode = -400 }

object TypeDescriptorWarpAction extends WarpAction { override val actionCode = 500 }
object OptionalTypeDescriptorWarpAction extends WarpAction { override val actionCode = -500 }