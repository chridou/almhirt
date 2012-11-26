package almhirt.riftwarp.impl.dematerializers

import org.joda.time.DateTime
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

object ToJsonCordDematerializerFuns {
  def launderString(str: String): Cord = Cord(str.replaceAll(""""""", """\""""))
}

class ToJsonCordDematerializer(state: Cord)(implicit hasDecomposers: HasDecomposers, hasDematerializers: HasDematerializers) extends ToCordDematerializer(RiftJson(), ToolGroup.StdLib) {
  import ToJsonCordDematerializerFuns._

  def dematerialize = DimensionCord(('{' -: state :- '}')).success

  def addPart(ident: String, part: Cord): AlmValidation[ToJsonCordDematerializer] = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordDematerializer(completeCord).success
    else
      ToJsonCordDematerializer((state :- ',') ++ completeCord).success
  }

  def addStringLikePart(ident: String, part: Cord): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, '\"' -: part :- '\"')

  private val nullCord = Cord("null")

  private def addNonePart(ident: String): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, nullCord)

  private def addStringPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, launderString(value))

  private def addBooleanPart(ident: String, value: Boolean): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value.toString)

  private def addLongPart(ident: String, value: Long): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value.toString)

  private def addBigIntPart(ident: String, value: BigInt): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, value.toString)

  private def addFloatingPointPart(ident: String, value: Double): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value.toString)

  private def addBigDecimalPart(ident: String, value: BigDecimal): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, value.toString)

  private def addByteArrayPart(ident: String, value: Array[Byte]): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, '[' + value.mkString(",") + ']')

  private def addDateTimePart(ident: String, value: DateTime): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, value.toString())

  private def addUuidPart(ident: String, value: _root_.java.util.UUID): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, value.toString())

  private def addJsonPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value.toString())

  private def addXmlPart(ident: String, value: _root_.scala.xml.Node): AlmValidation[ToJsonCordDematerializer] =
    addStringLikePart(ident, value.toString())

  private def addComplexPart(ident: String, value: Cord): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value)

  def ifNoneAddNull[T](ident: String, valueOpt: Option[T], ifNotNull: (String, T) => AlmValidation[ToJsonCordDematerializer]): AlmValidation[ToJsonCordDematerializer] = {
    option.cata(valueOpt)(ifNotNull(ident, _), addNonePart(ident))
  }

  def addString(ident: String, aValue: String) = addStringPart(ident, aValue)
  def addOptionalString(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addString)

  def addBoolean(ident: String, aValue: Boolean) = addBooleanPart(ident, aValue)
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = ifNoneAddNull(ident: String, anOptionalValue, addBooleanPart)

  def addByte(ident: String, aValue: Byte) = addLongPart(ident, aValue)
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = ifNoneAddNull(ident: String, anOptionalValue, addByte)
  def addInt(ident: String, aValue: Int) = addLongPart(ident, aValue)
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = ifNoneAddNull(ident: String, anOptionalValue, addInt)
  def addLong(ident: String, aValue: Long) = addLongPart(ident, aValue)
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = ifNoneAddNull(ident: String, anOptionalValue, addLong)
  def addBigInt(ident: String, aValue: BigInt) = addBigIntPart(ident, aValue)
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = ifNoneAddNull(ident: String, anOptionalValue, addBigIntPart)

  def addFloat(ident: String, aValue: Float) = addFloatingPointPart(ident, aValue)
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = ifNoneAddNull(ident: String, anOptionalValue, addFloat)
  def addDouble(ident: String, aValue: Double) = addFloatingPointPart(ident, aValue)
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = ifNoneAddNull(ident: String, anOptionalValue, addDouble)
  def addBigDecimal(ident: String, aValue: BigDecimal) = addBigDecimalPart(ident, aValue)
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = ifNoneAddNull(ident: String, anOptionalValue, addBigDecimal)

  def addByteArray(ident: String, aValue: Array[Byte]) = addByteArrayPart(ident, aValue)
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addByteArray)
  def addBlob(ident: String, aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addStringLikePart(ident, theBlob)
  }
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBlob)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addDateTimePart(ident, aValue)
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = ifNoneAddNull(ident: String, anOptionalValue, addDateTime)

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addUuidPart(ident, aValue)
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = ifNoneAddNull(ident: String, anOptionalValue, addUuid)

  def addJson(ident: String, aValue: String) = addJsonPart(ident, aValue)
  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addJson)
  def addXml(ident: String, aValue: scala.xml.Node) = addXmlPart(ident, aValue)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = ifNoneAddNull(ident: String, anOptionalValue, addXml)

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToJsonCordDematerializer] = {
    decomposer.decompose(aComplexType)(ToJsonCordDematerializer()).bind(toEmbed =>
      toEmbed.asInstanceOf[ToJsonCordDematerializer].dematerialize).bind(json =>
      addComplexPart(ident, json.manifestation))
  }
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, addComplexType(decomposer))

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToJsonCordDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
    }
  }

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexType(x, y))

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    hasDematerializers.tryGetCanDematerializePrimitiveMA[M, A, DimensionCord](RiftJson()) match {
      case Some(cdmpma) => 
        cdmpma.dematerialize(ma).bind(dim => addPart(ident, dim.manifestation))
      case None => 
        UnspecifiedProblem("No primitive dematerializer found for M[A](%s[%s]) for ident '%s'".format(mM.erasure.getName(), mA.erasure.getName(), ident)).failure
    }

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addPrimitiveMA(x, y))

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)

}

object ToJsonCordDematerializer extends DematerializerFactory[DimensionCord]{
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply()(implicit hasDecomposers: HasDecomposers, hasDematerializers: HasDematerializers): ToJsonCordDematerializer = apply(Cord(""))
  def apply(state: Cord)(implicit hasDecomposers: HasDecomposers, hasDematerializers: HasDematerializers): ToJsonCordDematerializer = new ToJsonCordDematerializer(state)
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasDematerializers: HasDematerializers): AlmValidation[Dematerializer[DimensionCord]] =
    apply().success
}
