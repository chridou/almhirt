package almhirt.riftwarp.impl

import almhirt.riftwarp._

class UnsafeRematerializationArrayFactoryRegistry extends HasRematerializationArrayFactories with HasRematerializersForHKTs {
  import scala.collection.mutable._
  private val toolregistry = HashMap[ToolGroup, HashMap[String, HashMap[String, AnyRef]]]()
  private val channelregistry = collection.mutable.HashMap[String, collection.mutable.HashMap[String, AnyRef]]()
  private val canRematerializePrimitiveMARegistry = collection.mutable.HashMap[String, AnyRef]()

  def addArrayFactory[R <: RematerializationArrayFactory[TDimension, TChannel], TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](arrayFactory: RematerializationArrayFactory[TDimension, TChannel], isChannelDefault: Boolean = false)(implicit mD: Manifest[TDimension], mC: Manifest[TChannel]) {
    val identDim = mD.erasure.getName
    val identCh = mC.erasure.getName

    if (!toolregistry.contains(arrayFactory.descriptor.toolGroup))
      toolregistry += (arrayFactory.descriptor.toolGroup -> HashMap[String, HashMap[String, AnyRef]]())
    val tooltypeentry = toolregistry(arrayFactory.descriptor.toolGroup)
    if (!tooltypeentry.contains(identCh))
      tooltypeentry += (identCh -> HashMap[String, AnyRef]())
    val channelEntry = tooltypeentry(identCh)
    channelEntry += (identDim -> arrayFactory.asInstanceOf[AnyRef])

    if (!channelregistry.contains(identCh))
      channelregistry += (identCh -> collection.mutable.HashMap[String, AnyRef]())
    val channeltypeentry = channelregistry(identCh)
    if (isChannelDefault || !channeltypeentry.contains(identDim))
      channeltypeentry += (identDim -> arrayFactory)
  }

  def tryGetArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](implicit mD: Manifest[TDimension], mC: Manifest[TChannel]): Option[RematerializationArrayFactory[TDimension, TChannel]] = {
    val identDim = mD.erasure.getName
    val identCh = mC.erasure.getName
    for {
      entry <- channelregistry.get(identCh)
      dematerializer <- entry.get(identDim)
    } yield dematerializer.asInstanceOf[RematerializationArrayFactory[TDimension, TChannel]]
  }
  
  def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension, TChannel <: RiftChannelDescriptor](crsma: CanRematerializePrimitiveMA[M, A, TDimension, TChannel]) {
    canRematerializePrimitiveMARegistry += ("%s-%s-%s-%s".format(crsma.tM.getName(), crsma.tA.getName(), crsma.tDimension.getName(), crsma.tChannel.getName()) -> crsma)
  }
  def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tDimension: Class[_ <: RiftDimension], tChannel: Class[_ <: RiftChannelDescriptor]): Option[AnyRef] =
    canRematerializePrimitiveMARegistry.get("%s-%s-%s-%s".format(tM.getName(), tA.getName(), tDimension.getName(), tChannel.getName()))
  
}