package almhirt.riftwarp.impl.dematerializers

import org.joda.time.DateTime
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

class ToJsonCordDematerializer private (state: Cord)(implicit hasDecomposers: HasDecomposers) extends DematerializesToCord {
  val descriptor = RiftFullDescriptor(RiftJson, ToolGroupStdLib)
  def dematerialize = ('{' -: state :- '}').success

  private def launderString(str: String): Cord = Cord(str.replaceAll(""""""", """\""""))

  def addPart(ident: String, part: Cord): AlmValidation[DematerializationFunnel] = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordDematerializer(completeCord).success
    else
      ToJsonCordDematerializer((state :- ',') ++ completeCord).success
  }
  
  def addStringLikePart(ident: String, part: Cord): AlmValidation[DematerializationFunnel] =
    addPart(ident, '\"' -: part :- '\"')

  private val nullCord = Cord("null")
  
  private def addNonePart(ident: String): AlmValidation[DematerializationFunnel] =
    addPart(ident, nullCord)

  private def addStringPart(ident: String, value: String): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, launderString(value))
 
  private def addBooleanPart(ident: String, value: Boolean): AlmValidation[DematerializationFunnel] =
    addPart(ident, value.toString)

  private def addLongPart(ident: String, value: Long): AlmValidation[DematerializationFunnel] =
    addPart(ident, value.toString)

  private def addBigIntPart(ident: String, value: BigInt): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, value.toString)

  private def addFloatingPointPart(ident: String, value: Double): AlmValidation[DematerializationFunnel] =
    addPart(ident, value.toString)

  private def addBigDecimalPart(ident: String, value: BigDecimal): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, value.toString)

  private def addByteArrayPart(ident: String, value: Array[Byte]): AlmValidation[DematerializationFunnel] =
    addPart(ident, "[" + value.mkString(",") + ']')

  private def addDateTimePart(ident: String, value: DateTime): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, value.toString())

  private def addUuidPart(ident: String, value: _root_.java.util.UUID): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, value.toString())

  private def addJsonPart(ident: String, value: String): AlmValidation[DematerializationFunnel] =
    addPart(ident, value.toString())

  private def addXmlPart(ident: String, value: _root_.scala.xml.Node): AlmValidation[DematerializationFunnel] =
    addStringLikePart(ident, value.toString())

  private def addComplexPart(ident: String, value: Cord): AlmValidation[DematerializationFunnel] =
    addPart(ident, value)

  def ifNoneAddNull[T](ident: String, valueOpt: Option[T], ifNotNull: (String, T) => AlmValidation[DematerializationFunnel]): AlmValidation[DematerializationFunnel] = {
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
  def addBlob(ident: String, aValue: Array[Byte]) = addByteArrayPart(ident, aValue)
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBlob)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addDateTimePart(ident, aValue)
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = ifNoneAddNull(ident: String, anOptionalValue, addDateTime)
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addUuidPart(ident, aValue)
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]) = ifNoneAddNull(ident: String, anOptionalValue, addUuid)

  def addJson(ident: String, aValue: String) = addJsonPart(ident, aValue)
  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addJson)
  def addXml(ident: String, aValue: scala.xml.Node) = addXmlPart(ident, aValue)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = ifNoneAddNull(ident: String, anOptionalValue, addXml)

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[DematerializationFunnel] = {
    decomposer.decompose(aComplexType)(ToJsonCordDematerializer()).bind(toEmbed =>
      toEmbed.asInstanceOf[ToJsonCordDematerializer].dematerialize).bind(json =>
      addComplexPart(ident, json))
  }
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[DematerializationFunnel] = 
    ifNoneAddNull(ident: String, anOptionalComplexType, addComplexType(decomposer))

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[DematerializationFunnel] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'".format(ident)).failure
    }
  }
  
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[DematerializationFunnel] = 
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexType(x, y))

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)
    
}

object ToJsonCordDematerializer {
  def apply()(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = apply(Cord(""))
  def apply(state: Cord)(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = new ToJsonCordDematerializer(state)
}

//object ToJsonStringDematerializer {
//  def apply()(implicit hasDecomposers: HasDecomposers): DematerializesToString = apply(Cord(""))
//  def apply(state: Cord)(implicit hasDecomposers: HasDecomposers): DematerializesToString = {
//    val inner = ToJsonCordDematerializer(state)
//    new DematerializesToString {
//      val descriptor = RiftFullDescriptor(RiftJson, ToolGroupStdLib)
//      def dematerialize = ('{' -: state :- '}').success
//    }
//  }
//}