package riftwarp.impl.dematerializers

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import riftwarp.TypeHelpers

class ToMapDematerializer(state: Map[String, Any], val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToRawMapDematerializer(RiftMap(), ToolGroupRiftStd()) with NoneHasNoEffectDematerializationFunnel[DimensionRawMap] {
  protected def spawnNew(path: List[String]): AlmValidation[ToMapDematerializer] =
    ToMapDematerializer.apply(path, divertBlob).success

  def dematerialize: AlmValidation[DimensionRawMap] = DimensionRawMap(state).success

  protected def addValue(ident: String, aValue: Any): AlmValidation[ToMapDematerializer] =
    (ToMapDematerializer(state + (ident -> aValue), path, divertBlob)).success

  def addString(ident: String, aValue: String) = addValue(ident, aValue)

  def addBoolean(ident: String, aValue: Boolean) = addValue(ident, aValue)

  def addByte(ident: String, aValue: Byte) = addValue(ident, aValue)
  def addInt(ident: String, aValue: Int) = addValue(ident, aValue)
  def addLong(ident: String, aValue: Long) = addValue(ident, aValue)
  def addBigInt(ident: String, aValue: BigInt) = addValue(ident, aValue)

  def addFloat(ident: String, aValue: Float) = addValue(ident, aValue)
  def addDouble(ident: String, aValue: Double) = addValue(ident, aValue)
  def addBigDecimal(ident: String, aValue: BigDecimal) = addValue(ident, aValue)

  def addByteArray(ident: String, aValue: Array[Byte]) = addValue(ident, aValue)
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addValue(ident, base64)
  }
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = addValue(ident, aValue)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addValue(ident, aValue)

  def addUri(ident: String, aValue: _root_.java.net.URI) = addValue(ident, aValue)

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addValue(ident, aValue)

  def addJson(ident: String, aValue: String) = addValue(ident, aValue)
  def addXml(ident: String, aValue: scala.xml.Node) = addValue(ident, aValue)

  def addBlob(ident: String, aValue: Array[Byte]) =
    getDematerializedBlob(ident, aValue).bind(blobDemat =>
      blobDemat.dematerialize.bind(dim =>
        addValue(ident, dim.manifestation)))
  
  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
    spawnNew(ident).bind(demat =>
      decomposer.decompose(aComplexType)(demat).bind(toEmbed =>
        toEmbed.asInstanceOf[ToMapDematerializer].dematerialize).bind(theMapToEmbed =>
        addValue(ident, theMapToEmbed.manifestation)))
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
      case Some(fo) => addValue(ident, ma)
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
    }
  }

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    def mapA(a: A, idx: String): AlmValidationAP[Map[String, Any]] =
      spawnNew(idx :: ident :: path).bind(freshDemat =>
        decomposer.decompose(a)(freshDemat).bind(_.dematerialize.map(_.manifestation))).toAgg

    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        (fo: @unchecked) match {
          case fo:  LinearMAFunctions[M] =>
            fo.sequenceValidations(fo.mapi(ma)((a, i) =>
              mapA(a, "[" + i.toString + "]"))).bind(x =>
              addValue(ident, x))
          case fo: NonLinearMAFunctions[M] =>
            fo.sequenceValidations(fo.maps(ma)((a, s) =>
              mapA(a, "[" + s + "]"))).bind(x =>
              addValue(ident, x))
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
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).bind(x =>
      addValue(ident, x))
  }

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).bind(x =>
      addValue(ident, x))
  }

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] = {
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) => addValue(ident, aMap)
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
            case (a, b) =>
              spawnNew("[" + a.toString + "]" :: ident :: path).bind(freshDemat =>
                decomposer.decompose(b)(freshDemat).bind(dematerializer =>
                  dematerializer.dematerialize.map(m => (a, m.manifestation))))
          }.map(x => x.toAgg)
        val sequenced = validations.sequence
        sequenced.map(_.toMap).bind(x =>
          addValue(ident, x))
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
        sequenced.map(_.toMap).bind(x =>
          addValue(ident, x))
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
        x.toAgg).sequence.map(_.toMap).bind(x =>
        addValue(ident, x)),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[Map[String, Any]] =
    hasDecomposers.tryGetRawDecomposerForAny(toDecompose) match {
      case Some(decomposer) =>
        spawnNew(idx :: ident :: path).bind(freshDemat =>
          decomposer.decomposeRaw(toDecompose)(freshDemat).bind(_.dematerialize.map(_.manifestation)))
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass().getName())).failure
    }

  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Any] =
    boolean.fold(
      TypeHelpers.isPrimitiveValue(toDecompose),
      toDecompose.success,
      toDecompose match {
        case toDecomposeAsAnyRef: AnyRef =>
          hasDecomposers.getRawDecomposerForAny(toDecomposeAsAnyRef).bind(decomposer =>
            spawnNew(idx :: ident :: path).bind(freshDemat =>
              decomposer.decomposeRaw(toDecomposeAsAnyRef)(freshDemat).bind(_.dematerialize.map(_.manifestation))))
        case x =>
          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
      })

}

object ToMapDematerializer extends DematerializerFactory[DimensionRawMap] {
  val channel = RiftMap()
  val tDimension = classOf[DimensionRawMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupRiftStd()

  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = apply(Map.empty, Nil, divertBlob)
  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = apply(Map.empty, path, divertBlob)
  def apply(state: Map[String, Any], path: List[String] = Nil, divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToMapDematerializer = new ToMapDematerializer(state, path, divertBlob)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[Dematerializer[DimensionRawMap]] =
    apply(divertBlob).success
}