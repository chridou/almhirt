//package riftwarp.impl.dematerializers
//
//import language.higherKinds
//
//import scala.reflect.ClassTag
//import scala.xml.{ Node => XmlNode, Elem, Text, TopScope, Null, UnprefixedAttribute }
//import org.joda.time.DateTime
//import scalaz._, Scalaz._
//import scalaz.Cord
//import scalaz.Cord._
//import scalaz.std._
//import scalaz.syntax.validation
//import almhirt.almvalidation.kit._
//import almhirt.common._
//import riftwarp._
//import riftwarp.ma._
//import riftwarp.DimensionXmlElem
//import riftwarp.components._
//
//object ToXmlElemWarpSequencerFuns {
//  def primitiveMapperByType[A](implicit m: ClassTag[A]): AlmValidation[A => Elem] = {
//    val t = m.runtimeClass
//    if (t == classOf[String])
//      ((x: A) => <String>{ x.toString }</String>).success
//    else if (t == classOf[Boolean])
//      ((x: A) => <Boolean>{ x.toString }</Boolean>).success
//    else if (t == classOf[Byte])
//      ((x: A) => <Byte>{ x.toString }</Byte>).success
//    else if (t == classOf[Int])
//      ((x: A) => <Int>{ x.toString }</Int>).success
//    else if (t == classOf[Long])
//      ((x: A) => <Long>{ x.toString }</Long>).success
//    else if (t == classOf[BigInt])
//      ((x: A) => <BigInt>{ x.toString }</BigInt>).success
//    else if (t == classOf[Float])
//      ((x: A) => <Float>{ x.toString }</Float>).success
//    else if (t == classOf[Double])
//      ((x: A) => <Double>{ x.toString }</Double>).success
//    else if (t == classOf[BigDecimal])
//      ((x: A) => <BigDecimal>{ x.toString }</BigDecimal>).success
//    else if (t == classOf[DateTime])
//      ((x: A) => <DateTime>{ x.toString }</DateTime>).success
//    else if (t == classOf[_root_.java.util.UUID])
//      ((x: A) => <Uuid>{ x.toString }</Uuid>).success
//    else if (t == classOf[_root_.java.net.URI])
//      ((x: A) => <Uri>{ x.toString }</Uri>).success
//    else
//      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
//  }
//
//  def primitiveMapperForAny(lookupFor: Any): AlmValidation[Any => Elem] = {
//    if (lookupFor.isInstanceOf[String])
//      primitiveMapperByType[String].map(mapper => (x: Any) => mapper(x.asInstanceOf[String]))
//    else if (lookupFor.isInstanceOf[Boolean])
//      primitiveMapperByType[Boolean].map(mapper => (x: Any) => mapper(x.asInstanceOf[Boolean]))
//    else if (lookupFor.isInstanceOf[Byte])
//      primitiveMapperByType[Byte].map(mapper => (x: Any) => mapper(x.asInstanceOf[Byte]))
//    else if (lookupFor.isInstanceOf[Int])
//      primitiveMapperByType[Int].map(mapper => (x: Any) => mapper(x.asInstanceOf[Int]))
//    else if (lookupFor.isInstanceOf[Long])
//      primitiveMapperByType[Long].map(mapper => (x: Any) => mapper(x.asInstanceOf[Long]))
//    else if (lookupFor.isInstanceOf[BigInt])
//      primitiveMapperByType[BigInt].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigInt]))
//    else if (lookupFor.isInstanceOf[Float])
//      primitiveMapperByType[Float].map(mapper => (x: Any) => mapper(x.asInstanceOf[Float]))
//    else if (lookupFor.isInstanceOf[Double])
//      primitiveMapperByType[Double].map(mapper => (x: Any) => mapper(x.asInstanceOf[Double]))
//    else if (lookupFor.isInstanceOf[BigDecimal])
//      primitiveMapperByType[BigDecimal].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigDecimal]))
//    else if (lookupFor.isInstanceOf[DateTime])
//      primitiveMapperByType[DateTime].map(mapper => (x: Any) => mapper(x.asInstanceOf[DateTime]))
//    else if (lookupFor.isInstanceOf[_root_.java.util.UUID])
//      primitiveMapperByType[_root_.java.util.UUID].map(mapper => (x: Any) => mapper(x.asInstanceOf[_root_.java.util.UUID]))
//    else
//      UnspecifiedProblem("No mapper found for %s".format(lookupFor.getClass.getName())).failure
//  }
//
//  def createKeyValuePair(kv: (Elem, Elem)): Elem = {
//    Elem(null, "KeyValue", Null, TopScope, true, <k>{ kv._1 }</k>, <v>{ kv._2 }</v>)
//  }
//
//  def foldKeyValuePairs(items: scala.collection.immutable.Iterable[(Elem, Elem)])(implicit functionObjects: HasFunctionObjects): AlmValidation[Elem] =
//    functionObjects.getChannelFolder[Elem, Elem](RiftXml()).flatMap(folder =>
//      functionObjects.getMAFunctions[scala.collection.immutable.Iterable].flatMap(fo =>
//        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
//
//}
//
//class ToXmlElemWarpSequencer(state: Seq[XmlNode], val path: List[String], protected val divertBlob: BlobDivert, riftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseWarpSequencer[DimensionXmlElem](classOf[DimensionXmlElem], hasDecomposers, hasFunctionObjects) with NoneIsHandledUnified[DimensionXmlElem] with NoneIsOmmitted[DimensionXmlElem] {
//  val toolGroup = ToolGroupRiftStd()
//  val channel = RiftXml()
//
//  import ToXmlElemWarpSequencerFuns._
//
//  private def wrapComplexElem(ident: String, complex: Elem) = Elem(null, ident, Null, TopScope, true, complex)
//
//  protected override def insertWarpSequencer(ident: String, dematerializer: WarpSequencer[DimensionXmlElem]) = {
//    addElem(wrapComplexElem(ident, dematerializer.dematerialize.manifestation))
//  }
//
//  protected def spawnNew(path: List[String]): ToXmlElemWarpSequencer =
//    ToXmlElemWarpSequencer.apply(path, divertBlob)
//
//  protected def asElem(): Elem =
//    option.cata(riftDescriptor)(
//      td => Elem(null, td.unqualifiedName, new UnprefixedAttribute("riftDescriptor", td.toString, Null), TopScope, true, state: _*),
//      Elem(null, "Element", Null, TopScope, true, state: _*))
//
//  private def addElem(elem: Elem): ToXmlElemWarpSequencer =
//    new ToXmlElemWarpSequencer(state :+ elem, path, divertBlob, riftDescriptor)
//
//  private def createPrimitiveElem(ident: String, value: String) = Elem(null, ident, Null, TopScope, true, Text(value))
//
//  override def dematerialize: DimensionXmlElem = DimensionXmlElem(asElem)
//
//  override def addString(ident: String, aValue: String) = addElem(createPrimitiveElem(ident, aValue))
//
//  override def addBoolean(ident: String, aValue: Boolean) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addByte(ident: String, aValue: Byte) = addElem(createPrimitiveElem(ident, aValue.toString))
//  override def addInt(ident: String, aValue: Int) = addElem(createPrimitiveElem(ident, aValue.toString))
//  override def addLong(ident: String, aValue: Long) = addElem(createPrimitiveElem(ident, aValue.toString))
//  override def addBigInt(ident: String, aValue: BigInt) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addFloat(ident: String, aValue: Float) = addElem(createPrimitiveElem(ident, aValue.toString))
//  override def addDouble(ident: String, aValue: Double) = addElem(createPrimitiveElem(ident, aValue.toString))
//  override def addBigDecimal(ident: String, aValue: BigDecimal) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addByteArray(ident: String, aValue: Array[Byte]) = addElem(createPrimitiveElem(ident, aValue.mkString(",")))
//  override def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = addElem(createPrimitiveElem(ident, org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)))
//  override def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = addBase64EncodedByteArray(ident, aValue)
//
//  override def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addUri(ident: String, aValue: _root_.java.net.URI) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addUuid(ident: String, aValue: _root_.java.util.UUID) = addElem(createPrimitiveElem(ident, aValue.toString))
//
//  override def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier) =
//    getDematerializedBlob(ident, aValue, blobIdentifier).map(blobDemat =>
//      insertWarpSequencer(ident, blobDemat))
//
//  override def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToXmlElemWarpSequencer] =
//    primitiveMapperByType[A].flatMap(map =>
//      MAFuncs.map(ma)(x => map(x)).flatMap(m =>
//        MAFuncs.fold(this.channel)(m)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem])).map(elem =>
//        addElem(wrapComplexElem(ident, elem))))
//
//  override def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToXmlElemWarpSequencer] =
//    MAFuncs.mapiV(ma)((x, idx) => decomposer.decompose(x)(spawnNew(ident)).map(_.dematerialize.manifestation)).flatMap(m =>
//      MAFuncs.fold(this.channel)(m)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem])).map(elem =>
//      addElem(wrapComplexElem(ident, elem)))
//
//  override def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToXmlElemWarpSequencer] =
//    hasDecomposers.getDecomposer[A].toOption match {
//      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
//      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.runtimeClass.getName())).failure
//    }
//
//  override def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToXmlElemWarpSequencer] =
//    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).flatMap(complex =>
//      MAFuncs.fold(this.channel)(complex)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem]).map(elem =>
//        addElem(wrapComplexElem(ident, elem))))
//
//  override def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[ToXmlElemWarpSequencer] =
//    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).flatMap(complex =>
//      MAFuncs.fold(this.channel)(complex)(hasFunctionObjects, mM, manifest[Elem], manifest[Elem]).map(elem =>
//        addElem(wrapComplexElem(ident, elem))))
//
//  override def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    (TypeHelpers.isPrimitiveType(mA.runtimeClass), TypeHelpers.isPrimitiveType(mB.runtimeClass)) match {
//      case (true, true) =>
//        primitiveMapperByType[A].flatMap(mapA =>
//          primitiveMapperByType[B].map(mapB =>
//            aMap.map {
//              case (a, b) =>
//                (mapA(a), mapB(b))
//            }).flatMap(items =>
//            foldKeyValuePairs(items)).map(elem =>
//            addElem(wrapComplexElem(ident, elem))))
//      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure
//      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.runtimeClass.getName())).failure
//      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure
//    }
//
//  override def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.runtimeClass),
//      primitiveMapperByType[A].map(mapA =>
//        aMap.map {
//          case (a, b) =>
//            decomposeWithDecomposer("[" + a.toString + "]" :: ident :: Nil)(decomposer)(b).map(elem =>
//              (mapA(a), elem)).toAgg
//        }).flatMap(x =>
//        x.toList.sequence[AlmValidationAP, (Elem, Elem)]).flatMap(items =>
//        foldKeyValuePairs(items)).map(elem =>
//        addElem(wrapComplexElem(ident, elem))),
//      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
//
//  override def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    hasDecomposers.getDecomposer[B].flatMap(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))
//
//  override def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.runtimeClass),
//      primitiveMapperByType[A].flatMap(mapA =>
//        aMap.toList.map {
//          case (a, b) =>
//            mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
//        }.sequence.map(_.toMap).flatMap(items =>
//          foldKeyValuePairs(items)).map(elem =>
//          addElem(wrapComplexElem(ident, elem)))),
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
//
//  override def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.runtimeClass),
//      primitiveMapperByType[A].flatMap(mapA =>
//        aMap.toList.map {
//          case (a, b) =>
//            mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
//        }.sequence.map(_.toMap).flatMap(items =>
//          foldKeyValuePairs(items)).map(elem =>
//          addElem(wrapComplexElem(ident, elem)))),
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
//
//  override def addMapSkippingUnknownValues[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[ToXmlElemWarpSequencer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.runtimeClass),
//      primitiveMapperByType[A].flatMap(mapA =>
//        aMap.toList.map {
//          case (a, b) =>
//            mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
//        }.sequence.map(_.toMap).flatMap(items =>
//          foldKeyValuePairs(items)).map(elem =>
//          addElem(wrapComplexElem(ident, elem)))),
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
//
//  override def addRiftDescriptor(descriptor: RiftDescriptor): ToXmlElemWarpSequencer =
//    new ToXmlElemWarpSequencer(state, path, divertBlob, Some(descriptor))
//
//  private def decomposeWithDecomposer[T <: AnyRef](idxIdent: List[String])(decomposer: Decomposer[T])(what: T): AlmValidation[Elem] =
//    decomposer.decompose(what)(spawnNew(idxIdent ++ path)).map(demat =>
//      demat.dematerialize.manifestation)
//
//  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[Elem] =
//    hasDecomposers.getRawDecomposerFor(toDecompose).flatMap(decomposer =>
//      decomposer.decomposeRaw(toDecompose)(spawnNew(idx :: ident :: path)).map(x => x.asInstanceOf[ToXmlElemWarpSequencer].asElem))
//
//  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Elem] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveValue(toDecompose),
//      primitiveMapperForAny(toDecompose).map(mapper => mapper(toDecompose)),
//      toDecompose match {
//        case toDecomposeAsAnyRef: AnyRef =>
//          mapWithComplexDecomposerLookUp(idx, ident)(toDecomposeAsAnyRef)
//        case x =>
//          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
//      })
//
//}
//
//object ToXmlElemWarpSequencer extends WarpSequencerFactory[DimensionXmlElem] {
//  val channel = RiftXml()
//  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
//  val toolGroup = ToolGroupStdLib()
//  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemWarpSequencer = apply(Seq.empty, divertBlob)
//  def apply(state: Seq[XmlNode], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemWarpSequencer = apply(state, Nil, divertBlob)
//  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemWarpSequencer = apply(Seq.empty, path, divertBlob)
//  def apply(state: Seq[XmlNode], path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToXmlElemWarpSequencer = new ToXmlElemWarpSequencer(state, path, divertBlob, None)
//  def createWarpSequencer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[ToXmlElemWarpSequencer] =
//    apply(divertBlob).success
//}
