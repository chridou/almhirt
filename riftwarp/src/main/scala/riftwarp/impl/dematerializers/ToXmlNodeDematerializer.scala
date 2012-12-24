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
import scala.xml.{Node => XmlNode, Elem, Text}

object ToXmlElemDematerializerFuns {
// def launderString(str: String): String = {
//    val buf = new StringBuilder
//    for (i <- 0 until str.length) {
//      val c = str.charAt(i)
//      buf.append(c match {
//        case '"' => "\\\""
//        case '\\' => "\\\\"
//        case '\b' => "\\b"
//        case '\f' => "\\f"
//        case '\n' => "\\n"
//        case '\r' => "\\r"
//        case '\t' => "\\t"
//        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) => "\\u%04x".format(c: Int)
//        case c => c
//      })
//    }
//    buf.toString
//  }

  def launderString(str: String): String = str
  
//  val mapString = (value: String) => Cord(mapStringLike(launderString(value)))
//  val mapBoolean = (value: Boolean) => Cord(value.toString)
//  val mapLong = (value: Long) => Cord(value.toString)
//  val mapBigInt = (value: BigInt) => Cord(mapStringLike(value.toString))
//  val mapFloatingPoint = (value: Double) => Cord(value.toString)
//  val mapBigDecimal = (value: BigDecimal) => Cord(mapStringLike(value.toString))
//  val mapDateTime = (value: DateTime) => Cord(mapStringLike(value.toString))
//  val mapUuid = (value: _root_.java.util.UUID) => Cord(mapStringLike(value.toString))
  
}

class ToXmlElemDematerializer(state: Seq[XmlNode], val path: List[String], protected val divertBlob: BlobDivert, typeDescriptor: Option[TypeDescriptor])(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionXmlElem](classOf[DimensionXmlElem]) with NoneHasNoEffectDematerializationFunnel[DimensionXmlNode]{
  import ToXmlElemDematerializerFuns._
  
  protected def asElem(): Elem =
    option.cata(typeDescriptor)(
     td => Elem("", td.unqualifiedName, null, null, state: _*),
     Elem("", "Element", null, null, state: _*))
  
  private def addElem(elem: Elem): AlmValidation[ToXmlElemDematerializer] =
    new ToXmlElemDematerializer(state :+ elem, path, divertBlob, typeDescriptor).success
     
  def dematerialize: AlmValidation[DimensionXmlElem] = DimensionXmlElem(asElem).success
  
  def addString(ident: String, aValue: String) = addElem(Elem("", ident, null, null, Text(launderString(aValue))))

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer[TDimension]]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer[TDimension]]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer[TDimension]]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer[TDimension]]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer[TDimension]]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer[TDimension]]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer[TDimension]]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer[TDimension]]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  
  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TDimension]]

  def addUri(ident: String, aValue: _root_.java.net.URI): AlmValidation[Dematerializer[TDimension]]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TDimension]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TDimension]]
  
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[ToXmlElemDematerializer] =
    new ToXmlElemDematerializer(state, path, divertBlob, Some(descriptor)).success
}

object ToXmlElemDematerializer  extends DematerializerFactory[DimensionXmlElem] {
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
