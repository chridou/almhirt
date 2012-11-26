package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeRematerializationArrayFactoryRegistry extends HasRematerializationArrayFactories with HasRematerializersForHKTs {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannel, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[RiftChannel, collection.mutable.HashMap[String, AnyRef]]()
  private val canRematerializePrimitiveMARegistry = collection.mutable.HashMap[String, AnyRef]()

  def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) {
    val identDim = arrayFactory.tDimension.getName()
 
    if (!toolregistry.contains(arrayFactory.toolGroup))
      toolregistry += (arrayFactory.toolGroup -> HashMap[RiftChannel, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(arrayFactory.toolGroup)
    if (!tooltypeentry.contains(arrayFactory.channel))
      tooltypeentry += (arrayFactory.channel -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(arrayFactory.channel)
    channelEntry += (identDim -> arrayFactory.asInstanceOf[AnyRef])

    if (!channelregistry.contains(arrayFactory.channel))
      channelregistry += (arrayFactory.channel -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(arrayFactory.channel)
    if (isChannelDefault || !channeltypeentry.contains(identDim))
      channeltypeentry += (identDim -> arrayFactory)
  }

  def tryGetArrayFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]) = {
    val dimensionIdent = mD.erasure.getName
    (toolGroup match {
      case None =>
        for {
          entry <- channelregistry.get(channel)
          dematerializer <- entry.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[TDimension]]
      case Some(toolGroup) =>
        for {
          toolentries <- toolregistry.get(toolGroup)
          channelEntries <- toolentries.get(channel)
          dematerializer <- channelEntries.get(dimensionIdent)
        } yield dematerializer.asInstanceOf[RematerializationArrayFactory[TDimension]]
    })
  }
  
  def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension](crsma: CanRematerializePrimitiveMA[M, A, TDimension]) {
    canRematerializePrimitiveMARegistry += ("%s-%s-%s-%s".format(crsma.tM.getName(), crsma.tA.getName(), crsma.tDimension.getName(), crsma.channel) -> crsma)
  }
 def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], tDimension: Class[_ <: RiftDimension], channel: RiftChannel): Option[AnyRef] =
    canRematerializePrimitiveMARegistry.get("%s-%s-%s-%s".format(tM.getName(), tA.getName(), tDimension.getName(), channel))
}