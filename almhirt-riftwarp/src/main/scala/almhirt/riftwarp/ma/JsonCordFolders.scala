package almhirt.riftwarp.ma

import scalaz.Cord
import scalaz.Cord._
import almhirt.riftwarp._

trait ToMJsonCordFolder[M[_]] extends RegisterableToMDimensionFold[M, DimensionCord] {
  val channel = RiftJson()
  val tDim = classOf[DimensionCord]
  protected def toCord(body: { def mkString(sep: String): String}): DimensionCord =
    DimensionCord('[' -: body.mkString(",") :- ']')
}

trait ToListJsonCordFolder extends ToMJsonCordFolder[List] {
  val tM = classOf[List[_]]
  def fold(mDim: List[DimensionCord]): DimensionCord = toCord(mDim.map(_.manifestation))
}

trait ToVectorJsonCordFolder extends ToMJsonCordFolder[Vector] {
  val tM = classOf[Vector[_]]
  def fold(mDim: Vector[DimensionCord]): DimensionCord = toCord(mDim.map(_.manifestation))
}

trait ToSetJsonCordFolder extends ToMJsonCordFolder[Set] {
  val tM = classOf[Set[_]]
  def fold(mDim: Set[DimensionCord]): DimensionCord = toCord(mDim.map(_.manifestation))
}

trait ToIterableJsonCordFolder extends ToMJsonCordFolder[Iterable] {
  val tM = classOf[Iterable[_]]
  def fold(mDim: Iterable[DimensionCord]): DimensionCord = toCord(mDim.map(_.manifestation))
}

trait ToTreeJsonCordFolder extends ToMJsonCordFolder[scalaz.Tree] {
  val tM = classOf[scalaz.Tree[_]]
  def fold(mDim: scalaz.Tree[DimensionCord]): DimensionCord = sys.error("not implemented")
}

object ToMJsonCordFolders {
  object toListJsonCordFolder extends ToListJsonCordFolder
  object toVectorJsonCordFolder extends ToVectorJsonCordFolder
  object toSetJsonCordFolder extends ToSetJsonCordFolder
  object toIterableJsonCordFolder extends ToIterableJsonCordFolder
}