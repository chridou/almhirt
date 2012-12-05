package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.flatmap

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed
  def converters: HasDimensionConverters

  def lookUpDematerializerFactoryAndConverter[DimTarget <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mT: Manifest[DimTarget]): AlmValidation[(DematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findDematerializerFactory(converters: List[RawDimensionConverter]): AlmValidation[(DematerializerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("A DematerializerFactory converter found").failure
        case x :: xs =>
          option.cata(toolShed.tryGetDematerializerFactoryByType(x.tTarget)(channel, toolGroup))(
            factory => (factory, x).success,
            findDematerializerFactory(xs))
      }

    option.cata(toolShed.tryGetDematerializerFactory[DimTarget](channel))(
      some => (some, Nil).success,
      findDematerializerFactory(converters.getConvertersTo[DimTarget]).map(tuple => (tuple._1, List(tuple._2))))
  }

  def getDematerializationFun[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]): AlmValidation[(AnyRef) => AlmValidation[TDimension]] =
    lookUpDematerializerFactoryAndConverter[TDimension](channel, toolGroup).map(factoryAndConverters =>
      (what: AnyRef) =>
        barracks.getDecomposerForAny(what).bind(decomposer =>
          factoryAndConverters._1.createDematerializer(barracks, toolShed).bind(demat =>
            decomposer.decomposeRaw(what)(demat).bind(demat =>
              demat.dematerializeRaw.bind(dimDemat =>
                factoryAndConverters._2.foldLeft(dimDemat.success[Problem])((acc, converter) =>
                  acc.fold(prob => prob.failure, dim => converter.convertRaw(dim))).map(_.asInstanceOf[TDimension]))))))

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] = 
  	getDematerializationFun[TDimension](channel, toolGroup).bind(fun => fun(what))
    //  {
//    val typeDescriptor =
//      what match {
//        case htd: HasTypeDescriptor => htd.typeDescriptor
//        case x => TypeDescriptor(x.getClass)
//      }
//    val decomposer = barracks.tryGetRawDecomposer(typeDescriptor)
//    val dematerializerV =
//      toolShed.tryGetDematerializerFactory[TDimension](channel, toolGroup).map(factory =>
//        factory.createDematerializer(barracks, toolShed)).validationOut
//    dematerializerV.bind(dematerializer =>
//      (decomposer, dematerializer) match {
//        case (Some(dec), Some(dem)) =>
//          dec.decomposeRaw(what)(dem).bind(funnel =>
//            almCast[RawDematerializer](funnel).bind(demat =>
//              demat.dematerializeRaw.map(_.asInstanceOf[TDimension])))
//        case (None, Some(_)) => UnspecifiedProblem("No decomposer found for type '%s'".format(typeDescriptor)).failure
//        case (Some(_), None) => UnspecifiedProblem("No dematerializer found for warping through '%s' into a '%s'".format(channel, m.erasure.getName())).failure
//        case (None, None) => UnspecifiedProblem("No decomposer found for type '%s' and no dematerializer found for warping through '%s' into a '%s'".format(typeDescriptor, channel, m.erasure.getName())).failure
//      })
//  }

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] = {
    implicit val hasRecomposers = barracks
    implicit val hasRecomposersForKKTs = toolShed
    toolShed.tryGetRematerializationArray[TDimension](warpStream)(channel).bind {
      case Some(array) =>
        array.tryGetTypeDescriptor.bind { descFromArray =>
          val typeDescriptor = descFromArray.getOrElse(TypeDescriptor(mtarget.erasure))
          barracks.tryGetRawRecomposer(typeDescriptor) match {
            case Some(recomposer) =>
              recomposer.recomposeRaw(array).bind(almCast[T](_))
            case None =>
              UnspecifiedProblem("No recomposer found for '%s'".format(typeDescriptor)).failure
          }
        }
      case None =>
        UnspecifiedProblem("No rematerialization array found for '%s' and from dimension '%s'".format(channel, mD.erasure.getName)).failure
    }
  }
}

object RiftWarp {
  def apply(theBarracks: RiftWarpBarracks, theToolShed: RiftWarpToolShed, theConverters: HasDimensionConverters): RiftWarp =
    new RiftWarp {
      val barracks = theBarracks
      val toolShed = theToolShed
      val converters = theConverters
    }

  def unsafe(): RiftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe, new impl.UnsafeDimensionConverterRegistry)
  def unsafeWithDefaults(): RiftWarp = {
    val riftWarp = unsafe()
    initializeWithDefaults(riftWarp)
    riftWarp
  }

  private def initializeWithDefaults(riftWarp: RiftWarp) {
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToMapDematerializer)
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToJsonCordDematerializer)

    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonStringRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonCordRematerializationArray)

    import riftwarp.ma._
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.listFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.vectorFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.setFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.iterableFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.treeFunctionObject)

    riftWarp.toolShed.addChannelFolder(JsonCordFolder)

    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToIterableConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToSetConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToVectorConverter)
  }
}