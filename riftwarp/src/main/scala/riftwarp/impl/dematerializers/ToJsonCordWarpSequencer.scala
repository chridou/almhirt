package riftwarp.impl.dematerializers

import scala.reflect.ClassTag
import scala.collection.IterableLike
import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ToJsonCordWarpSequencer(state: Cord, protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers) extends ToCordWarpSequencer(RiftJson(), ToolGroup.StdLib, ToJsonCordDematerializer, hasDecomposers) with NoneIsHandledUnified[DimensionCord] {
  private val nullCord = Cord("null")
  override def dematerialize = DimensionCord(('{' -: state :- '}'))
  
  protected def noneHandler(ident: String): ToJsonCordWarpSequencer = addPart(ident, nullCord)

  protected def spawnNew(): ToJsonCordWarpSequencer =
    ToJsonCordWarpSequencer.apply(divertBlob)

  protected override def addReprValue(ident: String, value: ValueRepr): WarpSequencer[DimensionCord] = addPart(ident, value)
  
  protected override def insertWarpSequencer(ident: String, warpSequencer: WarpSequencer[DimensionCord]) =
    addPart(ident, warpSequencer.dematerialize.manifestation)

  def addPart(ident: String, part: Cord): ToJsonCordWarpSequencer = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordWarpSequencer(completeCord, divertBlob)
    else
      ToJsonCordWarpSequencer((state :- ',') ++ completeCord, divertBlob)
  }

  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    addWith(RiftDescriptor.defaultKey, descriptor, riftwarp.serialization.common.RiftDescriptorDecomposer).forceResult
}

object ToJsonCordWarpSequencer extends WarpSequencerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = apply(Cord(""), divertBlob)
  def apply(state: Cord, divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = apply(state, divertBlob)
  def createWarpSequencer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): AlmValidation[ToJsonCordWarpSequencer] =
    apply(divertBlob).success
}
