package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import almhirt.util._

object PerformedCreateActionWarpPacker extends WarpPacker[PerformedCreateAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedCreateAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedCreateAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("aggRef", what.aggRef, AggregateRootRefWarpPacker)
  }
}

object PerformedUpdateActionWarpPacker extends WarpPacker[PerformedUpdateAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedUpdateAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedUpdateAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("aggRef", what.aggRef, AggregateRootRefWarpPacker)
  }
}

object PerformedDeleteActionWarpPacker extends WarpPacker[PerformedDeleteAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedDeleteAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedDeleteAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("aggRef", what.aggRef, AggregateRootRefWarpPacker)
  }
}

object PerformedNoActionWarpPacker extends WarpPacker[PerformedNoAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedNoAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedNoAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("reason", what.reason)
  }
}

object PerformedDomainActionWarpPacker extends WarpPacker[PerformedDomainAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedDomainAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedDomainAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case act: PerformedCreateAction => PerformedCreateActionWarpPacker(act)
      case act: PerformedUpdateAction => PerformedUpdateActionWarpPacker(act)
      case act: PerformedDeleteAction => PerformedDeleteActionWarpPacker(act)
      case act: PerformedNoAction => PerformedNoActionWarpPacker(act)
    }
  }
}

object PerformedActionWarpPacker extends WarpPacker[PerformedAction] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PerformedAction])
  val alternativeWarpDescriptors = Nil
  override def pack(what: PerformedAction)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case act: PerformedDomainAction => PerformedDomainActionWarpPacker(act)
    }
  }
}

object PerformedCreateActionWarpUnpacker extends RegisterableWarpUnpacker[PerformedCreateAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedCreateAction])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[PerformedCreateAction] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("aggRef", AggregateRootRefWarpUnpacker).map(PerformedCreateAction.apply)
  }
}

object PerformedUpdateActionWarpUnpacker extends RegisterableWarpUnpacker[PerformedUpdateAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedUpdateAction])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[PerformedUpdateAction] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("aggRef", AggregateRootRefWarpUnpacker).map(PerformedUpdateAction.apply)
  }
}

object PerformedDeleteActionWarpUnpacker extends RegisterableWarpUnpacker[PerformedDeleteAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedDeleteAction])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[PerformedDeleteAction] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("aggRef", AggregateRootRefWarpUnpacker).map(PerformedDeleteAction.apply)
  }
}

object PerformedNoActionWarpUnpacker extends RegisterableWarpUnpacker[PerformedNoAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedNoAction])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[PerformedNoAction] =
    withFastLookUp(from) { lookup =>
      lookup.getAs[String]("reason").map(PerformedNoAction)
  }
}

object PerformedDomainActionWarpUnpacker extends  RegisterableWarpUnpacker[PerformedDomainAction] with DivertingWarpUnpacker[PerformedDomainAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedDomainAction])
  val alternativeWarpDescriptors = Nil
  val divert =
    Map(
      PerformedCreateActionWarpUnpacker.warpDescriptor -> PerformedCreateActionWarpUnpacker,
      PerformedUpdateActionWarpUnpacker.warpDescriptor -> PerformedUpdateActionWarpUnpacker,
      PerformedDeleteActionWarpUnpacker.warpDescriptor -> PerformedDeleteActionWarpUnpacker,
      PerformedNoActionWarpUnpacker.warpDescriptor -> PerformedNoActionWarpUnpacker).lift
}

object PerformedActionWarpUnpacker extends RegisterableWarpUnpacker[PerformedAction] with DivertingWarpUnpacker[PerformedAction] {
  val warpDescriptor = WarpDescriptor(classOf[PerformedAction])
  val alternativeWarpDescriptors = Nil
  val divert =
    Map(
      PerformedCreateActionWarpUnpacker.warpDescriptor -> PerformedCreateActionWarpUnpacker,
      PerformedUpdateActionWarpUnpacker.warpDescriptor -> PerformedUpdateActionWarpUnpacker,
      PerformedDeleteActionWarpUnpacker.warpDescriptor -> PerformedDeleteActionWarpUnpacker,
      PerformedNoActionWarpUnpacker.warpDescriptor -> PerformedNoActionWarpUnpacker).lift
}