package riftwarp.util

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

trait CollectionWireSerializer[T]{ self : CustomWireSerializerTemplate[Seq[T]] =>
  type TT = T

  override protected def packOuter(in: Seq[TT]): AlmValidation[WarpPackage] = {
    val mapped = in.toVector.map(item => packInner(item).toAgg).sequence
    mapped.map(WarpCollection(_))
  }

  override protected def unpackOuter(out: WarpPackage): AlmValidation[Seq[TT]] = {
    out.to[WarpCollection].flatMap { wc =>
      val mapped = wc.items.map(item => unpackInner(item).toAgg).sequence
      mapped
    }
  }
}