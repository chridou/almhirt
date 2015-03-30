package riftwarp.serialization.common

import almhirt.common._
import riftwarp._
import riftwarp.std.kit._

import almhirt.configuration.Params

object ParamsWarpPackaging extends WarpPacker[Params] with RegisterableWarpPacker with RegisterableWarpUnpacker[Params] {
  override val warpDescriptor = WarpDescriptor(classOf[Params].getSimpleName())
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Params]) :: Nil
  override def pack(what: Params)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~> MLookUpForgiving[String, Any]("mappings", what.toMap)

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Params] =
    withFastLookUp(from) { lu ⇒
      for {
        valuesMap ← lu.getAssocs[String]("mappings").map(_.toMap)
      } yield Params.fromMap(valuesMap)
    }
}