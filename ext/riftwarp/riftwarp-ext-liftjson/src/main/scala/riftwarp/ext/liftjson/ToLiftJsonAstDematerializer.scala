//package riftwarp.ext.liftjson
//
//import org.joda.time.DateTime
//import scalaz.syntax.validation._
//import almhirt.common._
//import riftwarp._
//import riftwarp.ma._
//import net.liftweb.json._
//
//object LiftJsonAstDematerializerFuns {
//  import net.liftweb.json.JsonDSL._
//
//  val mapString = (value: String) => JString(value)
//  val mapBoolean = (value: Boolean) => JBool(value)
//  val mapInt = (value: Long) => JInt(value)
//  val mapBigInt = (value: BigInt) => JString(value.toString)
//  val mapFloatingPoint = (value: Double) => JDouble(value)
//  val mapBigDecimal = (value: BigDecimal) => JString(value.toString)
//  val mapDateTime = (value: DateTime) => JString(value.toString)
//  val mapUuid = (value: _root_.java.util.UUID) => JString(value.toString)
//
//  def primitiveMapperByType[A](implicit m: Manifest[A]): AlmValidation[A => JValue] = {
//    val t = m.erasure
//    if (t == classOf[String])
//      (mapString).asInstanceOf[A => JValue].success
//    else if (t == classOf[Boolean])
//      (mapBoolean).asInstanceOf[A => JValue].success
//    else if (t == classOf[Byte])
//      ((x: Byte) => mapInt(x)).asInstanceOf[A => JValue].success
//    else if (t == classOf[Int])
//      mapInt.asInstanceOf[A => JValue].success
//    else if (t == classOf[Long])
//      ((x: Long) => mapInt(x.toInt)).asInstanceOf[A => JValue].success
//    else if (t == classOf[BigInt])
//      (mapBigInt).asInstanceOf[A => JValue].success
//    else if (t == classOf[Float])
//      ((x: Float) => mapFloatingPoint(x)).asInstanceOf[A => JValue].success
//    else if (t == classOf[Double])
//      (mapFloatingPoint).asInstanceOf[A => JValue].success
//    else if (t == classOf[BigDecimal])
//      (mapBigDecimal).asInstanceOf[A => JValue].success
//    else if (t == classOf[DateTime])
//      (mapDateTime).asInstanceOf[A => JValue].success
//    else if (t == classOf[_root_.java.util.UUID])
//      (mapUuid).asInstanceOf[A => JValue].success
//    else
//      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
//  }
//
//  def mapperForAny(lookupFor: Any): AlmValidation[Any => JValue] = {
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
//  def createKeyValuePair(kv: (JValue, JValue)): JValue = {
//    ("k" -> kv._1) ~ ("v" -> kv._2 )
//  }
//
//  def foldKeyValuePairs(items: scala.collection.immutable.Iterable[(JValue, JValue)])(implicit functionObjects: HasFunctionObjects): AlmValidation[JArray] =
//    functionObjects.getChannelFolder[JValue, JArray](RiftJson()).bind(folder =>
//      functionObjects.getMAFunctions[scala.collection.immutable.Iterable].bind(fo =>
//        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
//}
//
//
//class ToLiftJsonAstDematerializer(state: List[JField])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends Dematerializer[DimensionLiftJsonAst] {
//  val channel = RiftJson()
//  val tDimension = classOf[DimensionLiftJsonAst]
//  val toolGroup = ToolGroupLiftJson()
//
//  import LiftJsonAstDematerializerFuns._
//  
//  def dematerialize: AlmValidation[DimensionLiftJsonAst] = DimensionLiftJsonAst(JObject(state.reverse)).success
//
//  def addString(ident: String, aValue: String) = ToLiftJsonAstDematerializer(JField(ident, mapString(aValue)) :: state).success
//
//  def addBoolean(ident: String, aValue: Boolean) = ToLiftJsonAstDematerializer(JField(ident, mapBoolean(aValue)) :: state).success
//
//  def addByte(ident: String, aValue: Byte) = ToLiftJsonAstDematerializer(JField(ident, mapInt(aValue)) :: state).success
//  def addInt(ident: String, aValue: Int) = ToLiftJsonAstDematerializer(JField(ident, mapInt(aValue)) :: state).success
//  def addLong(ident: String, aValue: Long) = ToLiftJsonAstDematerializer(JField(ident, mapInt(aValue)) :: state).success
//  def addBigInt(ident: String, aValue: BigInt) = ToLiftJsonAstDematerializer(JField(ident, mapBigInt(aValue)) :: state).success
//
//  def addFloat(ident: String, aValue: Float) = ToLiftJsonAstDematerializer(JField(ident, mapFloatingPoint(aValue)) :: state).success
//  def addDouble(ident: String, aValue: Double) = ToLiftJsonAstDematerializer(JField(ident, mapFloatingPoint(aValue)) :: state).success
//  def addBigDecimal(ident: String, aValue: BigDecimal) = ToLiftJsonAstDematerializer(JField(ident, mapBigDecimal(aValue)) :: state).success
//
//  def addByteArray(ident: String, aValue: Array[Byte]) = 
//     ToLiftJsonAstDematerializer(JField(ident, JArray(aValue.map(v => JInt(v.toInt)).toList)) :: state).success
//     
//  def addBlob(ident: String, aValue: Array[Byte]) = {
//    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
//    ToLiftJsonAstDematerializer(JField(ident, mapString(theBlob)) :: state).success
//  }
//
//  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = ToLiftJsonAstDematerializer(JField(ident, mapDateTime(aValue)) :: state).success
//
//  def addUuid(ident: String, aValue: _root_.java.util.UUID) = ToLiftJsonAstDematerializer(JField(ident, mapUuid(aValue)) :: state).success
//
//  def addJson(ident: String, aValue: String) = ToLiftJsonAstDematerializer(JField(ident, mapBoolean(aValue)) :: state).success
//  def addXml(ident: String, aValue: scala.xml.Node) = ToLiftJsonAstDematerializer(JField(ident, mapString(aValue.toString)) :: state).success
//
//  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
//    decomposer.decompose(aComplexType)(ToMapDematerializer()).bind(toEmbed =>
//      toEmbed.asInstanceOf[ToMapDematerializer].dematerialize).map(theMapToEmbed =>
//      ToMapDematerializer(state + (ident -> theMapToEmbed.manifestation)))
//  }
//
//  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToMapDematerializer] = {
//    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
//      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
//      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
//    }
//  }
//
//  def addComplexTypeFixed[U <: AnyRef](ident: String, aComplexType: U)(implicit mU: Manifest[U]): AlmValidation[ToMapDematerializer] =
//    hasDecomposers.getDecomposer[U].bind(decomposer => addComplexType(decomposer)(ident, aComplexType))
//  
//  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
//    hasFunctionObjects.tryGetMAFunctions[M] match {
//      case Some(fo) =>
//        (ToMapDematerializer(state + (ident -> ma))).success
//      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
//    }
//  }
//
//  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
//    hasFunctionObjects.tryGetMAFunctions[M] match {
//      case Some(fo) =>
//        val mapped = fo.map(ma)(elem =>
//          (decomposer.decompose(elem)(ToMapDematerializer()).bind(_.dematerialize).toAgg))
//        fo.sequenceValidations(mapped)
//          .map(x => fo.map(x)(_.manifestation))
//          .map(x => ToMapDematerializer(state + (ident -> x)))
//      case None => UnspecifiedProblem("No function object  found for ident '%s' and M[_](%s[_])".format(ident, mM.erasure.getName())).failure
//    }
//  }
//
//  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] =
//    hasDecomposers.tryGetDecomposer[A] match {
//      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
//      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
//    }
//
//  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
//    MAFuncs.mapV(ma)(mapWithComplexDecomposerLookUp(ident)).bind(ma =>
//      (ToMapDematerializer(state + (ident -> ma))).success)
//  }
//
//  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToMapDematerializer] = {
//    MAFuncs.mapV(ma)(mapWithPrimitiveAndComplexDecomposerLookUp(ident)).map(ma =>
//      ToMapDematerializer(state + (ident -> ma)))
//  }
//
//  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] = {
//    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
//      case (true, true) => ToMapDematerializer(state + (ident -> map)).success
//      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
//      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
//      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
//    }
//  }
//
//  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.erasure),
//      {
//        val validations =
//          aMap.toList.map {
//            case (a, b) => decomposer.decompose(b)(ToMapDematerializer()).bind(dematerializer =>
//              dematerializer.dematerialize.map(m => (a, m.manifestation)))
//          }.map(x => x.toAgg)
//        val sequenced = validations.sequence
//        sequenced.map(_.toMap).map(x => ToMapDematerializer(state + (ident -> x)))
//      },
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)
//
//  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
//    hasDecomposers.getDecomposer[B].bind(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))
//
//  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.erasure),
//      {
//        val validations =
//          aMap.toList.map {
//            case (a, b) => mapWithComplexDecomposerLookUp(ident)(b).map(b => (a, b))
//          }.map(x => x.toAgg)
//        val sequenced = validations.sequence
//        sequenced.map(_.toMap).map(x => ToMapDematerializer(state + (ident -> x)))
//      },
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)
//
//  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToMapDematerializer] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveType(mA.erasure),
//          aMap.toList.map { case (a, b) => 
//            mapWithPrimitiveAndComplexDecomposerLookUp(ident)(b).map(m => 
//              (a, b))}.map(x => 
//                x.toAgg).sequence.map(_.toMap).map(x => 
//                  ToMapDematerializer(state + (ident -> x))),
//      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)
//
//  def addTypeDescriptor(descriptor: TypeDescriptor) = (ToMapDematerializer(state + (TypeDescriptor.defaultKey -> descriptor))).success
//
//  private def mapWithComplexDecomposerLookUp(ident: String)(toDecompose: AnyRef): AlmValidation[Map[String, Any]] =
//    hasDecomposers.tryGetRawDecomposer(toDecompose.getClass) match {
//      case Some(decomposer) =>
//        decomposer.decomposeRaw(toDecompose)(ToMapDematerializer()).bind(_.dematerialize.map(_.manifestation))
//      case None =>
//        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass().getName())).failure
//    }
//
//  private def mapWithPrimitiveAndComplexDecomposerLookUp(ident: String)(toDecompose: Any): AlmValidation[Any] =
//    boolean.fold(
//      TypeHelpers.isPrimitiveValue(toDecompose),
//      toDecompose.success,
//      toDecompose match {
//        case toDecomposeAsAnyRef: AnyRef =>
//          option.cata(hasDecomposers.tryGetRawDecomposer(toDecomposeAsAnyRef.getClass))(
//            decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef)(ToMapDematerializer()).bind(_.dematerialize.map(_.manifestation)),
//            UnspecifiedProblem("No decomposer or primitive mapper found for ident '%s'. i was trying to find a match for '%s'".format(ident, toDecompose.getClass.getName())).failure)
//        case x =>
//          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
//      })
//  
//}
//
//object ToLiftJsonAstDematerializer {
//  def apply(state: List[JField])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToLiftJsonAstDematerializer = new ToLiftJsonAstDematerializer(state)
//  def apply()(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToLiftJsonAstDematerializer = apply(List.empty)
//}