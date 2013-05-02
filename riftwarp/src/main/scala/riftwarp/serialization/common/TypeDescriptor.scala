//package riftwarp.serialization.common
//
//import scalaz._, Scalaz._
//import almhirt.common._
//import almhirt.almvalidation.kit._
//import riftwarp._
//
//object WarpDescriptorDecomposer extends Decomposer[WarpDescriptor] {
//  val warpDescriptor = WarpDescriptor(classOf[WarpDescriptor])
//  val alternativeWarpDescriptors = Nil
//  def decompose[TDimension <: RiftDimension](what: WarpDescriptor, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
//    into.addString("identifier", what.identifier).addOptionalInt("version", what.version).ok
//  }
//}
//
//object WarpDescriptorRecomposer extends Recomposer[WarpDescriptor] {
//  val warpDescriptor = WarpDescriptor(classOf[WarpDescriptor])
//  val alternativeWarpDescriptors = Nil
//  def recompose(from: Extractor): AlmValidation[WarpDescriptor] = {
//    val identifier = from.getString("identifier").toAgg
//    val version = from.tryGetInt("version").toAgg
//    (identifier |@| version)(WarpDescriptor.apply)
//  }
//}