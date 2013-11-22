package riftwarp.util

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

trait CollectionWireSerializer[TIn, TOut] extends CustomWireSerializer[Seq[TIn], Seq[TOut]] {
  type TTIn = TIn
  type TTOut = TOut

  override protected def packOuter(in: Seq[TTIn]): AlmValidation[WarpPackage] = {
    val mapped = in.toVector.map(item => packInner(item).toAgg).sequence
    mapped.map(WarpCollection(_))
  }

  override protected def unpackOuter(out: WarpPackage): AlmValidation[Seq[TTOut]] = {
    out.to[WarpCollection].flatMap { wc =>
      val mapped = wc.items.map(item => unpackInner(item).toAgg).sequence
      mapped
    }
  }
}