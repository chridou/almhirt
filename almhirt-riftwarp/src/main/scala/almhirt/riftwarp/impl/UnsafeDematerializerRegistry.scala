package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeDematerializerRegistry extends HasDematerializers {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[String, collection.mutable.HashMap[String, AnyRef]]()

  private val canDematerializePrimitiveMARegistry = collection.mutable.HashMap[String, AnyRef]()

  def addDematerializer[D <: Dematerializer[_, _], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel, To], asChannelDefault: Boolean)(implicit m: Manifest[To]) {
    val identifier = m.erasure.getName

    if (!toolregistry.contains(dematerializer.descriptor.toolGroup))
      toolregistry += (dematerializer.descriptor.toolGroup -> HashMap[RiftChannelDescriptor, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(dematerializer.descriptor.toolGroup)
    if (!tooltypeentry.contains(dematerializer.descriptor.channelType))
      tooltypeentry += (dematerializer.descriptor.channelType -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(dematerializer.descriptor.channelType)
    channelEntry += (identifier -> dematerializer.asInstanceOf[AnyRef])

    if (!channelregistry.contains(dematerializer.descriptor.channelType.getClass().getName()))
      channelregistry += (dematerializer.descriptor.channelType.getClass().getName() -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(dematerializer.descriptor.channelType.getClass().getName())
    if (asChannelDefault || !channeltypeentry.contains(identifier))
      channeltypeentry += (identifier -> dematerializer)
  }

  def tryGetDematerializerByDescriptor[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[_, To]] = {
    val identifier = m.erasure.getName
    (warpType match {
      case ct: RiftChannelDescriptor =>
        for {
          entry <- channelregistry.get(ct.getClass().getName())
          dematerializer <- entry.get(identifier)
        } yield dematerializer.asInstanceOf[Dematerializer[_, To]]
      case fd: RiftFullDescriptor =>
        for {
          toolentries <- toolregistry.get(fd.toolGroup)
          channelEntries <- toolentries.get(fd.channelType)
          dematerializer <- channelEntries.get(identifier)
        } yield dematerializer.asInstanceOf[Dematerializer[_, To]]
    })
  }

  def tryGetDematerializer[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](implicit md: Manifest[To], mc: Manifest[TChannel]): Option[Dematerializer[TChannel, To]] =
    for {
      entry <- channelregistry.get(mc.erasure.getName())
      dematerializer <- entry.get(md.erasure.getName())
    } yield dematerializer.asInstanceOf[Dematerializer[TChannel, To]]

  def addCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension](cdsma: CanDematerializePrimitiveMA[M, A, TChannel, TDimension]) =
    canDematerializePrimitiveMARegistry += ("%s-%s-%s-%s".format(cdsma.tM.getName(), cdsma.tA.getName(), cdsma.tChannel.getName(), cdsma.tDimension.getName()) -> cdsma)
  def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], tChannel: Class[_ <: RiftChannelDescriptor], tDimension: Class[_ <: RiftDimension]) =
    canDematerializePrimitiveMARegistry.get("%s-%s-%s-%s".format(tM.getName(), tA.getName(), tChannel.getName(), tDimension.getName()))

}