package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeDematerializerRegistry extends HasDematerializers {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannel, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()

  private val canDematerializePrimitiveMARegistry = collection.mutable.HashMap[String, AnyRef]()

  def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean) {
    val dimensionIdent = factory.tDimension.getName()

    if (!toolregistry.contains(factory.toolGroup))
      toolregistry += (factory.toolGroup -> HashMap[RiftChannel, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(factory.toolGroup)
    if (!tooltypeentry.contains(factory.channel))
      tooltypeentry += (factory.channel -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(factory.channel)
    channelEntry += (dimensionIdent -> factory.asInstanceOf[AnyRef])

    if (!channelregistry.contains(factory.channel))
      channelregistry += (factory.channel -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(factory.channel)
    if (asChannelDefault || !channeltypeentry.contains(dimensionIdent))
      channeltypeentry += (dimensionIdent -> factory)
  }

  def tryGetDematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]): Option[DematerializerFactory[TDimension]] = {
    val dimensionIdent = mD.erasure.getName
    (toolGroup match {
      case None =>
        for {
          entry <- channelregistry.get(channel)
          dematerializer <- entry.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[DematerializerFactory[TDimension]]
      case Some(toolGroup) =>
        for {
          toolentries <- toolregistry.get(toolGroup)
          channelEntries <- toolentries.get(channel)
          dematerializer <- channelEntries.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[DematerializerFactory[TDimension]]
    })
  }

  def addCanDematerializePrimitiveMA[M[_], A](cdsma: CanDematerializePrimitiveMA[M, A, _ <: RiftDimension]) {
    canDematerializePrimitiveMARegistry += ("%s-%s-%s-%s".format(cdsma.tM.getName(), cdsma.tA.getName(), cdsma.channel, cdsma.tDimension.getName()) -> cdsma)
  }
  def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], channel: RiftChannel, tDimension: Class[_ <: RiftDimension]) =
    canDematerializePrimitiveMARegistry.get("%s-%s-%s-%s".format(tM.getName(), tA.getName(), channel, tDimension.getName()))

}