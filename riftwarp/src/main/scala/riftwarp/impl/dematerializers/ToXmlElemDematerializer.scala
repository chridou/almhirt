package riftwarp.impl.dematerializers

import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.ma._
import riftwarp.TypeHelpers
import scala.xml.{ Node => XmlNode, Elem, Text, TopScope, Null, UnprefixedAttribute }
import riftwarp.DimensionXmlElem

object ToXmlElemDematerializerFuns {
  def primitiveMapperByType[A](implicit m: Manifest[A]): AlmValidation[A => Elem] = {
    val t = m.erasure
    if (t == classOf[String])
      ((x: A) => <String>{ x.toString }</String>).success
    else if (t == classOf[Boolean])
      ((x: A) => <Boolean>{ x.toString }</Boolean>).success
    else if (t == classOf[Byte])
      ((x: A) => <Byte>{ x.toString }</Byte>).success
    else if (t == classOf[Int])
      ((x: A) => <Int>{ x.toString }</Int>).success
    else if (t == classOf[Long])
      ((x: A) => <Long>{ x.toString }</Long>).success
    else if (t == classOf[BigInt])
      ((x: A) => <BigInt>{ x.toString }</BigInt>).success
    else if (t == classOf[Float])
      ((x: A) => <Float>{ x.toString }</Float>).success
    else if (t == classOf[Double])
      ((x: A) => <Double>{ x.toString }</Double>).success
    else if (t == classOf[BigDecimal])
      ((x: A) => <BigDecimal>{ x.toString }</BigDecimal>).success
    else if (t == classOf[DateTime])
      ((x: A) => <DateTime>{ x.toString }</DateTime>).success
    else if (t == classOf[_root_.java.util.UUID])
      ((x: A) => <Uuid>{ x.toString }</Uuid>).success
    else if (t == classOf[_root_.java.net.URI])
      ((x: A) => <Uri>{ x.toString }</Uri>).success
    else
      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
  }

  def primitiveMapperForAny(lookupFor: Any): AlmValidation[Any => Elem] = {
    if (lookupFor.isInstanceOf[String])
      primitiveMapperByType[String].map(mapper => (x: Any) => mapper(x.asInstanceOf[String]))
    else if (lookupFor.isInstanceOf[Boolean])
      primitiveMapperByType[Boolean].map(mapper => (x: Any) => mapper(x.asInstanceOf[Boolean]))
    else if (lookupFor.isInstanceOf[Byte])
      primitiveMapperByType[Byte].map(mapper => (x: Any) => mapper(x.asInstanceOf[Byte]))
    else if (lookupFor.isInstanceOf[Int])
      primitiveMapperByType[Int].map(mapper => (x: Any) => mapper(x.asInstanceOf[Int]))
    else if (lookupFor.isInstanceOf[Long])
      primitiveMapperByType[Long].map(mapper => (x: Any) => mapper(x.asInstanceOf[Long]))
    else if (lookupFor.isInstanceOf[BigInt])
      primitiveMapperByType[BigInt].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigInt]))
    else if (lookupFor.isInstanceOf[Float])
      primitiveMapperByType[Float].map(mapper => (x: Any) => mapper(x.asInstanceOf[Float]))
    else if (lookupFor.isInstanceOf[Double])
      primitiveMapperByType[Double].map(mapper => (x: Any) => mapper(x.asInstanceOf[Double]))
    else if (lookupFor.isInstanceOf[BigDecimal])
      primitiveMapperByType[BigDecimal].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigDecimal]))
    else if (lookupFor.isInstanceOf[DateTime])
      primitiveMapperByType[DateTime].map(mapper => (x: Any) => mapper(x.asInstanceOf[DateTime]))
    else if (lookupFor.isInstanceOf[_root_.java.util.UUID])
      primitiveMapperByType[_root_.java.util.UUID].map(mapper => (x: Any) => mapper(x.asInstanceOf[_root_.java.util.UUID]))
    else
      UnspecifiedProblem("No mapper found for %s".format(lookupFor.getClass.getName())).failure
  }

  def createKeyValuePair(kv: (Elem, Elem)): Elem = {
    Elem(null, "KeyValue", Null, TopScope, kv._1, kv._2)
  }

  def foldKeyValuePairs(items: scala.collection.immutable.Iterable[(Elem, Elem)])(implicit functionObjects: HasFunctionObjects): AlmValidation[Elem] =
    functionObjects.getChannelFolder[Elem, Elem](RiftXml()).bind(folder =>
      functionObjects.getMAFunctions[scala.collection.immutable.Iterable].bind(fo =>
        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))

}

class ToXmlElemDematerializer(state: Seq[XmlNode], val path: List[String], protected val divertBlob: BlobDivert, typeDescriptor: Option[TypeDescriptor])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionXmlElem](classOf[DimensionXmlElem]) with NoneHasNoEffectDematerializationFunnel[DimensionXmlElem] {
  val toolGroup = ToolGroupRiftStd()
  val channel = RiftXml()

  
  import ToXmlElemDematerializerFuns._

  protected def spawnNew(path: List[String]): AlmValidation[ToXmlElemDematerializer] =
    ToXmlElemDematerializer.apply(path, divertBlob).success

  protected def asElem(): Elem =
    option.cata(typeDescriptor)(
      td => Elem(null, td.unqualifiedName, new UnprefixedAttribute("typedescriptor", td.toString, Null), TopScope, state: _*),
      Elem(null, "Element", Null, TopScope, state: _*))

  private def addElem(elem: Elem): AlmValidation[ToXmlElemDematerializer] =
    new ToXmlElemDematerializer(state :+ elem, path, divertBlob, typeDescriptor).success

  def createPrimitiveElem(ident: String, value: String) = Elem(null, ident, Null, TopScope, Text(value))
  def wrapComplexElem(ident: String, complex: Elem) = Elem(null, ident, Null, TopScope, complex)

  def dematerialize: AlmValidation[DimensionXmlElem] = DimensionXmlElem(asElem).success

  def addString(ident: String, aValue: String) = addElem(createPrimitiveElem(ident, aValue))

  def addBoolean(ident: String, aValue: Boolean) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addByte(ident: String, aValue: Byte) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addInt(ident: String, aValue: Int) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addLong(ident: String, aValue: Long) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addBigInt(ident: String, aValue: BigInt) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addFloat(ident: String, aValue: Float) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addDouble(ident: String, aValue: Double) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addBigDecimal(ident: String, aValue: BigDecimal) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addByteArray(ident: String, aValue: Array[Byte]) = addElem(createPrimitiveElem(ident, aValue.mkString(",")))
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = addElem(createPrimitiveElem(ident, org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)))
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = addBase64EncodedByteArray(ident, aValue)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addUri(ident: String, aValue: _root_.java.net.URI) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addElem(createPrimitiveElem(ident, aValue.toString))

  def addJson(ident: String, aValue: String) = addElem(createPrimitiveElem(ident, aValue.toString))
  def addXml(ident: String, aValue: scala.xml.Node) = NotSupportedProblem("addXml is not supported").withIdentifier(ident).failure

  def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier) =
    getDematerializedBlob(ident, aValue, blobIdentifier).bind(blobDemat =>
      blobDemat.dematerialize.bind(elem =>
        addElem(wrapComplexElem(ident, elem.manifestation))))

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToXmlElemDematerializer] =
    spawnNew(ident).bind(demat =>
      decomposer.decompose(aComplexType)(demat).bind(toEmbed =>
        toEmbed.dematerialize).bind(elem =>
        addElem(wrapComplexElem(ident, elem.manifestation))))

  def addComplexTypeFixed[U <: AnyRef](ident: String, aComplexType: U)(implicit mU: Manifest[U]): AlmValidation[ToXmlElemDematerializer] =
    hasDecomposers.getDecomposer[U].bind(decomposer => addComplexType(decomposer)(ident, aComplexType))

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToXmlElemDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, aComplexType.getClass.getName())).failure
    }
  }

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToXmlElemDematerializer] =
    primitiveMapperByType[A].bind(map =>
      MAFuncs.map(ma)(x => map(x)).bind(m =>
        MAFuncs.fold(this.channel)(m)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem])).bind(elem =>
        addElem(wrapComplexElem(ident, elem))))

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToXmlElemDematerializer] =
    spawnNew(ident).bind(demat =>
      MAFuncs.mapiV(ma)((x, idx) => decomposer.decompose(x)(demat).bind(_.dematerialize.map(_.manifestation))).bind(m =>
        MAFuncs.fold(this.channel)(m)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem])).bind(elem =>
        addElem(wrapComplexElem(ident, elem))))

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToXmlElemDematerializer] =
    hasDecomposers.tryGetDecomposer[A] match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
    }

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToXmlElemDematerializer] =
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem]).bind(elem =>
        addElem(wrapComplexElem(ident, elem))))

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToXmlElemDematerializer] =
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem]).bind(elem =>
        addElem(wrapComplexElem(ident, elem))))

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToXmlElemDematerializer] =
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) =>
        primitiveMapperByType[A].bind(mapA =>
          primitiveMapperByType[B].map(mapB =>
            aMap.map {
              case (a, b) => (mapA(a), mapB(b))
            }).bind(items =>
            foldKeyValuePairs(items).bind(elem =>
              addElem(wrapComplexElem(ident, elem)))))
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }

  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToXmlElemDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].map(mapA =>
        aMap.map {
          case (a, b) =>
            decomposeWithDecomposer("[" + a.toString + "]" :: ident :: Nil)(decomposer)(b).map(elem =>
              (mapA(a), elem)).toAgg
        }).bind(x =>
        x.toList.sequence[AlmValidationAP, (Elem, Elem)]).bind(items =>
        foldKeyValuePairs(items)).bind(elem =>
        addElem(wrapComplexElem(ident, elem))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToXmlElemDematerializer] =
    hasDecomposers.getDecomposer[B].bind(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))
      
  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToXmlElemDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].bind(mapA =>
        aMap.toList.map {
          case (a, b) =>
            mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
        }.sequence.map(_.toMap).bind(items =>
          foldKeyValuePairs(items)).bind(elem =>
          addElem(wrapComplexElem(ident, elem)))),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToXmlElemDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].bind(mapA =>
        aMap.toList.map {
          case (a, b) =>
            mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
        }.sequence.map(_.toMap).bind(items =>
          foldKeyValuePairs(items)).bind(elem =>
          addElem(wrapComplexElem(ident, elem)))),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)
      
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[ToXmlElemDematerializer] =
    new ToXmlElemDematerializer(state, path, divertBlob, Some(descriptor)).success

  private def decomposeWithDecomposer[T <: AnyRef](idxIdent: List[String])(decomposer: Decomposer[T])(what: T): AlmValidation[Elem] =
    spawnNew(idxIdent ++ path).bind(demat =>
      decomposer.decompose(what)(demat).bind(demat =>
        demat.dematerialize.map(_.manifestation)))

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[Elem] =
    hasDecomposers.getRawDecomposerForAny(toDecompose).bind(decomposer =>
      spawnNew(idx :: ident :: path).bind(freshDemat =>
        decomposer.decomposeRaw(toDecompose)(freshDemat).map(x => x.asInstanceOf[ToXmlElemDematerializer].asElem)))

  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Elem] =
    boolean.fold(
      TypeHelpers.isPrimitiveValue(toDecompose),
      primitiveMapperForAny(toDecompose).map(mapper => mapper(toDecompose)),
      toDecompose match {
        case toDecomposeAsAnyRef: AnyRef =>
          mapWithComplexDecomposerLookUp(idx, ident)(toDecomposeAsAnyRef)
        case x =>
          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
      })

}

object ToXmlElemDematerializer extends DematerializerFactory[DimensionXmlElem] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemDematerializer = apply(Seq.empty, divertBlob)
  def apply(state: Seq[XmlNode], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemDematerializer = apply(state, Nil, divertBlob)
  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemDematerializer = apply(Seq.empty, path, divertBlob)
  def apply(state: Seq[XmlNode], path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemDematerializer = new ToXmlElemDematerializer(state, path, divertBlob, None)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[ToXmlElemDematerializer] =
    apply(divertBlob).success
}
