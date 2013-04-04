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
import almhirt.serialization._

class ToJsonCordWarpSequencer(state: Cord, collectedBlobReferences: Vector[ExtractedBlobReference], override val blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers) extends ToCordWarpSequencer(RiftJson(), ToolGroup.StdLib, collectedBlobReferences, hasDecomposers) with NoneIsHandledUnified[DimensionCord] {
  private val nullCord = Cord("null")
  val dematerializer = ToJsonCordDematerializer
  override def dematerialize = DimensionCord(('{' -: state :- '}'))
  
  protected def noneHandler(ident: String): ToJsonCordWarpSequencer = addPart(ident, nullCord)

  protected override def spawnNew(): ToJsonCordWarpSequencer =
    ToJsonCordWarpSequencer.apply(blobPolicy)

  protected override def addReprValue(ident: String, value: ValueRepr): WarpSequencer[DimensionCord] = addPart(ident, value)
  
  protected override def insertWarpSequencer(ident: String, warpSequencer: WarpSequencer[DimensionCord], collectedBlobReferences: Vector[ExtractedBlobReference]) =
    addPart(ident, warpSequencer.dematerialize.manifestation, collectedBlobReferences)

  def addPart(ident: String, part: Cord, collectedBlobReferences: Vector[ExtractedBlobReference] = this.collectedBlobReferences): ToJsonCordWarpSequencer = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordWarpSequencer(completeCord, collectedBlobReferences, blobPolicy)
    else
      ToJsonCordWarpSequencer((state :- ',') ++ completeCord, collectedBlobReferences, blobPolicy)
  }

  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    addWith(RiftDescriptor.defaultKey, descriptor, riftwarp.serialization.common.RiftDescriptorDecomposer).forceResult
}

object ToJsonCordWarpSequencer extends WarpSequencerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(state: Cord, collectedBlobReferences: Vector[ExtractedBlobReference], blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = 
    apply(state, blobPolicy)
  def apply(state: Cord, blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = 
    new ToJsonCordWarpSequencer(state, Vector.empty, blobPolicy)
  def apply(blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = 
    new ToJsonCordWarpSequencer(Cord.empty, Vector.empty, blobPolicy)
  def createWarpSequencer(blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers): AlmValidation[ToJsonCordWarpSequencer] =
    apply(blobPolicy).success
}
