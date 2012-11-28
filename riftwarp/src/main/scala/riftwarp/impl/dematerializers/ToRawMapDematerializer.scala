package riftwarp.impl.dematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._

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

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        (ToMapDematerializer(state + (ident -> ma))).success
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
    }
  }

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        val mapped = fo.map(ma)(elem =>
          (decomposer.decompose(elem)(ToMapDematerializer()).bind(_.dematerialize).toAgg))
        fo.sequenceValidations(mapped)
          .map(x => fo.map(x)(_.manifestation))
          .map(x => ToMapDematerializer(state + (ident -> x)))
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
    }
  }

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.tryGetDecomposer[A] match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
    }

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    def mapWithDecomposerLookUp(toDecompose: AnyRef): AlmValidation[Map[String, Any]] =
      hasDecomposers.tryGetRawDecomposer(toDecompose.getClass) match {
        case Some(decomposer) =>
          decomposer.decomposeRaw(toDecompose)(ToMapDematerializer()).bind(_.dematerialize.map(_.manifestation))
        case None =>
          UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
      }
    MAFuncs.mapV(ma)(mapWithDecomposerLookUp).bind(ma =>
      (ToMapDematerializer(state + (ident -> ma))).success)
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