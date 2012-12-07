package riftwarp.impl.dematerializers

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import riftwarp.TypeHelpers

class ToMapDematerializer(state: Map[String, Any], val path: List[String])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToRawMapDematerializer(RiftMap(), ToolGroupRiftStd()) with NoneHasNoEffectDematerializationFunnel[DimensionRawMap] {

  def dematerialize: AlmValidation[DimensionRawMap] = DimensionRawMap(state).success

  def addString(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addBoolean(ident: String, aValue: Boolean) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addByte(ident: String, aValue: Byte) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addInt(ident: String, aValue: Int) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addLong(ident: String, aValue: Long) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addBigInt(ident: String, aValue: BigInt) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addFloat(ident: String, aValue: Float) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addDouble(ident: String, aValue: Double) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addBigDecimal(ident: String, aValue: BigDecimal) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addByteArray(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addBase64String(ident: String, aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    (ToMapDematerializer(state + (ident -> base64), path)).success
  }
  def addBlob(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addJson(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue), path)).success
  def addXml(ident: String, aValue: scala.xml.Node) = (ToMapDematerializer(state + (ident -> aValue), path)).success

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
    decomposer.decompose(aComplexType)(ToMapDematerializer(ident :: path)).bind(toEmbed =>
      toEmbed.asInstanceOf[ToMapDematerializer].dematerialize).map(theMapToEmbed =>
      ToMapDematerializer(state + (ident -> theMapToEmbed.manifestation), path))
  }

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
    }
  }

  def addComplexTypeFixed[U <: AnyRef](ident: String, aComplexType: U)(implicit mU: Manifest[U]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.getDecomposer[U].bind(decomposer => addComplexType(decomposer)(ident, aComplexType))

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        (ToMapDematerializer(state + (ident -> ma), path)).success
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
    }
  }

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    def mapA(a: A, idx: String): AlmValidationAP[Map[String, Any]] =
      decomposer.decompose(a)(ToMapDematerializer(idx :: ident :: path)).bind(_.dematerialize.map(_.manifestation)).toAgg

    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        fo match {
          case fo: LinearMAFunctions[M] =>
            fo.sequenceValidations(fo.mapi(ma)((a, i) =>
              mapA(a, "[" + i.toString + "]"))).map(x =>
              ToMapDematerializer(state + (ident -> x), path))
          case fo: NonLinearMAFunctions[M] =>
            fo.sequenceValidations(fo.maps(ma)((a, s) =>
              mapA(a, "[" + s + "]"))).map(x =>
              ToMapDematerializer(state + (ident -> x), path))
          case x =>
            UnspecifiedProblem("Not yet supported: %s".format(x)).failure
        }
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
    }
  }

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.tryGetDecomposer[A] match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
    }

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).bind(ma =>
      (ToMapDematerializer(state + (ident -> ma), path)).success)
  }

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).map(ma =>
      ToMapDematerializer(state + (ident -> ma), path))
  }

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] = {
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) => ToMapDematerializer(state + (ident -> map), path).success
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }
  }

  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      {
        val validations =
          aMap.toList.map {
            case (a, b) => decomposer.decompose(b)(ToMapDematerializer("[" + a.toString + "]" :: ident :: path)).bind(dematerializer =>
              dematerializer.dematerialize.map(m => (a, m.manifestation)))
          }.map(x => x.toAgg)
        val sequenced = validations.sequence
        sequenced.map(_.toMap).map(x => ToMapDematerializer(state + (ident -> x), path))
      },
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.getDecomposer[B].bind(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))

  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      {
        val validations =
          aMap.toList.map {
            case (a, b) => mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (a, b))
          }.map(x => x.toAgg)
        val sequenced = validations.sequence
        sequenced.map(_.toMap).map(x => ToMapDematerializer(state + (ident -> x), path))
      },
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      aMap.toList.map {
        case (a, b) =>
          mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(m =>
            (a, b))
      }.map(x =>
        x.toAgg).sequence.map(_.toMap).map(x =>
        ToMapDematerializer(state + (ident -> x), path)),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addTypeDescriptor(descriptor: TypeDescriptor) = (ToMapDematerializer(state + (TypeDescriptor.defaultKey -> descriptor), path)).success

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[Map[String, Any]] =
    hasDecomposers.tryGetRawDecomposerForAny(toDecompose) match {
      case Some(decomposer) =>
        decomposer.decomposeRaw(toDecompose)(ToMapDematerializer(idx :: ident :: path)).bind(_.dematerialize.map(_.manifestation))
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass().getName())).failure
    }

  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Any] =
    boolean.fold(
      TypeHelpers.isPrimitiveValue(toDecompose),
      toDecompose.success,
      toDecompose match {
        case toDecomposeAsAnyRef: AnyRef =>
          option.cata(hasDecomposers.tryGetRawDecomposer(toDecomposeAsAnyRef.getClass))(
            decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef)(ToMapDematerializer(idx :: ident :: path)).bind(_.dematerialize.map(_.manifestation)),
            UnspecifiedProblem("No decomposer or primitive mapper found for ident '%s'. i was trying to find a match for '%s'".format(ident, toDecompose.getClass.getName())).failure)
        case x =>
          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
      })

}

object ToMapDematerializer extends DematerializerFactory[DimensionRawMap] {
  val channel = RiftMap()
  val tDimension = classOf[DimensionRawMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupRiftStd()

  def apply()(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = apply(Map.empty, Nil)
  def apply(path: List[String])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = apply(Map.empty, path)
  def apply(state: Map[String, Any], path: List[String] = Nil)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = new ToMapDematerializer(state, path)
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[Dematerializer[DimensionRawMap]] =
    apply().success
}