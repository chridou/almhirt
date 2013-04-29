package riftwarp.impl.rematerializers

import java.util.{ UUID => JUUID }
import java.net.{ URI => JURI }
import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.xml.{ Elem => XmlElem, NodeSeq }
import org.joda.time.DateTime
import scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.xml.all._
import riftwarp._

object FromStdLibXmlRematerializer extends RematerializerTemplate[XmlElem] {
  override def rematerialize(what: XmlElem): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: XmlElem): AlmValidation[WarpPackage] =
    what.label match {
      case "value" => extractPrimitiveFromElem(what)
      case "bytes" => extractBytes(what)
      case "base64" => extractBase64(what)
      case "collection" => extractCollection(what)
      case _ => extractObject(what)
    }

  private def extractObject(what: XmlElem): AlmValidation[WarpObject] =
    ???

  private def extractCollection(what: XmlElem): AlmValidation[WarpCollection] =
    ???

  private def extractBytes(what: XmlElem): AlmValidation[WarpBytes] =
    ???
    
  private def extractBase64(what: XmlElem): AlmValidation[WarpBytes] =
    ???
  
    
  def unescapeString(str: String): String = {
    //scala.xml.Utility.unescape(str, new StringBuilder).result
    str
  }

  private def extractPrimitiveFromElem(value: XmlElem): AlmValidation[WarpPrimitive] =
    (value \@ "type").fold(
      fail => BadDataProblem(s"Could not extract a primitive from an XmlElem because it lacks an attribute 'type' which specifies the contained data type. The element was: ${value.label}").failure,
      succ => succ match {
        case "String" => extractString(value)
        case "Boolean" => extractBoolean(value)
        case "Byte" => extractByte(value)
        case "Int" => extractInt(value)
        case "Long" => extractLong(value)
        case "BigInt" => extractBigInt(value)
        case "Float" => extractFloat(value)
        case "Double" => extractDouble(value)
        case "BigDecimal" => extractBigDecimal(value)
        case "DateTime" => extractDateTime(value)
        case "Uuid" => extractUuid(value)
        case "Uri" => extractUri(value)
        case x => BadDataProblem(s"Could not extract a primitive from an XmlElem because its attribute 'type' specifies an unknown data type '$x'. The element was: ${value.label}").failure
      })
  
  private def extractString(value: XmlElem): AlmValidation[WarpPrimitive] = WarpString(unescapeString(value.text)).success
  private def extractBoolean(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractBoolean.map(WarpBoolean(_))
  private def extractByte(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractByte.map(WarpByte(_))
  private def extractInt(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractInt.map(WarpInt(_))
  private def extractLong(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractLong.map(WarpLong(_))
  private def extractBigInt(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractBigInt.map(WarpBigInt(_))
  private def extractFloat(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractFloat.map(WarpFloat(_))
  private def extractDouble(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDouble.map(WarpDouble(_))
  private def extractBigDecimal(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDecimal.map(WarpBigDecimal(_))
  private def extractDateTime(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDateTime.map(WarpDateTime(_))
  private def extractUuid(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractUuid.map(WarpUuid(_))
  private def extractUri(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractUri.map(WarpUri(_))
    
}