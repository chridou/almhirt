package almhirt.riftwarp.impl.dematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

class ToRawMapDematerializer(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers) extends DematerializesToRawMap[RiftMap] with NoneHasNoEffectDematerializationFunnel[DimensionRawMap, RiftMap] {
  val descriptor = RiftFullDescriptor(RiftMap(), ToolGroupRiftStd())
  def dematerialize: AlmValidation[DimensionRawMap] = DimensionRawMap(state).success

  def addString(ident: String, aValue: String) = (ToRawMapDematerializer(state + (ident -> aValue))).success

  def addBoolean(ident: String, aValue: Boolean) = (ToRawMapDematerializer(state + (ident -> aValue))).success

  def addByte(ident: String, aValue: Byte) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addInt(ident: String, aValue: Int) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addLong(ident: String, aValue: Long) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addBigInt(ident: String, aValue: BigInt) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  
  def addFloat(ident: String, aValue: Float) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addDouble(ident: String, aValue: Double) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addBigDecimal(ident: String, aValue: BigDecimal) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  
  def addByteArray(ident: String, aValue: Array[Byte]) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addBlob(ident: String, aValue: Array[Byte]) = (ToRawMapDematerializer(state + (ident -> aValue))).success

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID) = (ToRawMapDematerializer(state + (ident -> aValue))).success

  def addJson(ident: String, aValue: String) = (ToRawMapDematerializer(state + (ident -> aValue))).success
  def addXml(ident: String, aValue: scala.xml.Node) = (ToRawMapDematerializer(state + (ident -> aValue))).success

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToRawMapDematerializer] = {
    decomposer.decompose(aComplexType)(ToRawMapDematerializer()).bind(toEmbed =>
      toEmbed.asInstanceOf[ToRawMapDematerializer].dematerialize).map(theMapToEmbed =>
      ToRawMapDematerializer(state + (ident -> theMapToEmbed.manifestation)))
  }

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToRawMapDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
    }
  }
  
  def addTypeDescriptor(descriptor: TypeDescriptor) = (ToRawMapDematerializer(state + (TypeDescriptor.defaultKey -> descriptor))).success

  
}

object ToRawMapDematerializer {
  def apply()(implicit hasDecomposers: HasDecomposers): ToRawMapDematerializer = apply(Map.empty)
  def apply(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers): ToRawMapDematerializer = new ToRawMapDematerializer(state)
}