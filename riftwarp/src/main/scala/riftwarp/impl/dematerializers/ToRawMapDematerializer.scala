package riftwarp.impl.dematerializers

import language.higherKinds

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import riftwarp.components._

class ToMapDematerializer(state: Map[String, Any], val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToRawMapDematerializer(RiftMap(), ToolGroupRiftStd(), hasDecomposers, hasFunctionObjects) with NoneIsHandledUnified[DimensionRawMap] with NoneIsOmmitted[DimensionRawMap] {
   protected override def spawnNew(path: List[String]): AlmValidation[ToMapDematerializer] =
    ToMapDematerializer.apply(path, divertBlob).success

  override def dematerialize: DimensionRawMap = DimensionRawMap(state)

  protected override def insertDematerializer(ident: String, dematerializer: Dematerializer[DimensionRawMap]) =
    addValue(ident, dematerializer.dematerialize.manifestation)
  
  protected def addValue(ident: String, aValue: Any): AlmValidation[ToMapDematerializer] =
    (ToMapDematerializer(state + (ident -> aValue), path, divertBlob)).success

  override def addString(ident: String, aValue: String) = addValue(ident, aValue)

  override def addBoolean(ident: String, aValue: Boolean) = addValue(ident, aValue)

  override def addByte(ident: String, aValue: Byte) = addValue(ident, aValue)
  override def addInt(ident: String, aValue: Int) = addValue(ident, aValue)
  override def addLong(ident: String, aValue: Long) = addValue(ident, aValue)
  override def addBigInt(ident: String, aValue: BigInt) = addValue(ident, aValue)

  override def addFloat(ident: String, aValue: Float) = addValue(ident, aValue)
  override def addDouble(ident: String, aValue: Double) = addValue(ident, aValue)
  override def addBigDecimal(ident: String, aValue: BigDecimal) = addValue(ident, aValue)

  override def addByteArray(ident: String, aValue: Array[Byte]) = addValue(ident, aValue)
  override def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addValue(ident, base64)
  }
  override def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = addValue(ident, aValue)

  override def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addValue(ident, aValue)

  override def addUri(ident: String, aValue: _root_.java.net.URI) = addValue(ident, aValue)

  override def addUuid(ident: String, aValue: _root_.java.util.UUID) = addValue(ident, aValue)

  override def addJson(ident: String, aValue: String) = addValue(ident, aValue)
  override def addXml(ident: String, aValue: scala.xml.Node) = addValue(ident, aValue)

  override def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier) =
    getDematerializedBlob(ident, aValue, blobIdentifier).flatMap(blobDemat =>
      addValue(ident, blobDemat.dematerialize.manifestation))

  override def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToMapDematerializer] = {
    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) => addValue(ident, ma)
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.runtimeClass.getName())).failure
    }
  }

  override def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToMapDematerializer] = {
    def mapA(a: A, idx: String): AlmValidationAP[Map[String, Any]] =
      spawnNew(idx :: ident :: path).flatMap(freshDemat =>
        decomposer.decompose(a)(freshDemat).map(_.dematerialize.manifestation)).toAgg

    hasFunctionObjects.tryGetMAFunctions[M] match {
      case Some(fo) =>
        (fo: @unchecked) match {
          case fo: LinearMAFunctions[M] =>
            fo.sequenceValidations(fo.mapi(ma)((a, i) =>
              mapA(a, "[" + i.toString + "]"))).flatMap(x =>
              addValue(ident, x))
          case fo: NonLinearMAFunctions[M] =>
            fo.sequenceValidations(fo.maps(ma)((a, s) =>
              mapA(a, "[" + s + "]"))).flatMap(x =>
              addValue(ident, x))
          case x =>
            UnspecifiedProblem("Not yet supported: %s".format(x)).failure
        }
      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.runtimeClass.getName())).failure
    }
  }

  override def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.getDecomposer[A](mA.runtimeClass).toOption match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.runtimeClass.getName())).failure
    }

  override def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToMapDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).flatMap(x =>
      addValue(ident, x))
  }

  override def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToMapDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).flatMap(x =>
      addValue(ident, x))
  }

  override def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] = {
    (TypeHelpers.isPrimitiveType(mA.runtimeClass), TypeHelpers.isPrimitiveType(mB.runtimeClass)) match {
      case (true, true) => addValue(ident, aMap)
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.runtimeClass.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure
    }
  }

  override def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      {
        val validations =
          aMap.toList.map {
            case (a, b) =>
              spawnNew("[" + a.toString + "]" :: ident :: path).flatMap(freshDemat =>
                decomposer.decompose(b)(freshDemat).map(dematerializer =>
                  (a, dematerializer.dematerialize.manifestation)))
          }.map(x => x.toAgg)
        val sequenced = validations.sequence
        sequenced.map(_.toMap).flatMap(x =>
          addValue(ident, x))
      },
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  override def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] =
    hasDecomposers.getDecomposer[B](mB.runtimeClass).flatMap(decomposer => 
      addComplexMap[A, B](decomposer)(ident, aMap))

  override def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      {
        val validations =
          aMap.toList.map {
            case (a, b) => mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (a, b))
          }.map(x => x.toAgg)
        val sequenced = validations.sequence
        sequenced.map(_.toMap).flatMap(x =>
          addValue(ident, x))
      },
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  override def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      aMap.toList.map {
        case (a, b) =>
          mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(m =>
            (a, b))
      }.map(x =>
        x.toAgg).sequence.map(_.toMap).flatMap(x =>
        addValue(ident, x)),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  override def addMapSkippingUnknownValues[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToMapDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      aMap.toList.map {
        case (a, b) =>
          mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(m =>
            (a, b))
      }.map(x =>
        x.toAgg).sequence.map(_.toMap).flatMap(x =>
        addValue(ident, x)),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
      
  override def addRiftDescriptor(descriptor: RiftDescriptor) = addString(RiftDescriptor.defaultKey, descriptor.toString)

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[Map[String, Any]] =
    hasDecomposers.getRawDecomposerFor(toDecompose).toOption match {
      case Some(decomposer) =>
        spawnNew(idx :: ident :: path).flatMap(freshDemat =>
          decomposer.decomposeRaw(toDecompose)(freshDemat).map(_.dematerialize.manifestation))
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass().getName())).failure
    }

  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Any] =
    boolean.fold(
      TypeHelpers.isPrimitiveValue(toDecompose),
      toDecompose.success,
      toDecompose match {
        case toDecomposeAsAnyRef: AnyRef =>
          hasDecomposers.getRawDecomposerFor(toDecomposeAsAnyRef).flatMap(decomposer =>
            spawnNew(idx :: ident :: path).flatMap(freshDemat =>
              decomposer.decomposeRaw(toDecomposeAsAnyRef)(freshDemat).map(_.dematerialize.manifestation)))
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