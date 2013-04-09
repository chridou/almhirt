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

class ToJsonCordWarpSequencer(state: Cord)(implicit hasDecomposers: HasDecomposers) extends ToCordWarpSequencer(RiftJson(), ToolGroup.StdLib, hasDecomposers) with NoneIsHandledUnified[DimensionCord] {
  private val nullCord = Cord("null")
  val dematerializer = ToJsonCordDematerializer
  override def dematerialize = DimensionCord(('{' -: state :- '}'))
  
  protected def noneHandler(ident: String): ToJsonCordWarpSequencer = addPart(ident, nullCord)

  protected override def spawnNew(): ToJsonCordWarpSequencer =
    ToJsonCordWarpSequencer.apply()

  protected override def addReprValue(ident: String, value: ValueRepr): WarpSequencer[DimensionCord] = addPart(ident, value)
  
  protected override def insertWarpSequencer(ident: String, warpSequencer: WarpSequencer[DimensionCord]) =
    addPart(ident, warpSequencer.dematerialize.manifestation)

  def addPart(ident: String, part: Cord): ToJsonCordWarpSequencer = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordWarpSequencer(completeCord)
    else
      ToJsonCordWarpSequencer((state :- ',') ++ completeCord)
  }

  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    addWith(RiftDescriptor.defaultKey, descriptor, riftwarp.serialization.common.RiftDescriptorDecomposer).forceResult
}

object ToJsonCordWarpSequencer extends WarpSequencerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(state: Cord)(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = 
    new ToJsonCordWarpSequencer(state)
  def apply()(implicit hasDecomposers: HasDecomposers): ToJsonCordWarpSequencer = 
    new ToJsonCordWarpSequencer(Cord.empty)
  def createWarpSequencer(implicit hasDecomposers: HasDecomposers): AlmValidation[ToJsonCordWarpSequencer] =
    apply().success
}
