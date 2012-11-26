package almhirt.riftwarp.impl.dematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._
import almhirt.riftwarp.ma._

class ToMapDematerializer(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToRawMapDematerializer(RiftMap(), ToolGroupRiftStd()) with NoneHasNoEffectDematerializationFunnel[DimensionRawMap] {
  def dematerialize: AlmValidation[DimensionRawMap] = DimensionRawMap(state).success

  def addString(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addBoolean(ident: String, aValue: Boolean) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addByte(ident: String, aValue: Byte) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addInt(ident: String, aValue: Int) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addLong(ident: String, aValue: Long) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBigInt(ident: String, aValue: BigInt) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addFloat(ident: String, aValue: Float) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addDouble(ident: String, aValue: Double) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBigDecimal(ident: String, aValue: BigDecimal) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addByteArray(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBlob(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addJson(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addXml(ident: String, aValue: scala.xml.Node) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
    decomposer.decompose(aComplexType)(ToMapDematerializer()).bind(toEmbed =>
      toEmbed.asInstanceOf[ToMapDematerializer].dematerialize).map(theMapToEmbed =>
      ToMapDematerializer(state + (ident -> theMapToEmbed.manifestation)))
  }

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
    }
  }

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] =
    hasFunctionObjects.tryGetToMADimensionFunctor[M] match {
      case Some(_) => (ToMapDematerializer(state + (ident -> ma))).success
      case None => sys.error("")
    }

 
  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    hasFunctionObjects.tryGetToMADimensionFunctor[M] match {
      case Some(functor) => 
        val mDim = functor.map[A, DimensionRawMap](ma)(x => decomposer.decompose(x)(ToMapDematerializer()).bind(_.dematerialize))
        sys.error("")
      case None => sys.error("")
    }
  }

  def addTypeDescriptor(descriptor: TypeDescriptor) = (ToMapDematerializer(state + (TypeDescriptor.defaultKey -> descriptor))).success
}

object ToMapDematerializer extends DematerializerFactory[DimensionRawMap] {
  val channel = RiftMap()
  val tDimension = classOf[DimensionRawMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupRiftStd()

  def apply()(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = apply(Map.empty)
  def apply(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = new ToMapDematerializer(state)
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[Dematerializer[DimensionRawMap]] =
    apply().success
}