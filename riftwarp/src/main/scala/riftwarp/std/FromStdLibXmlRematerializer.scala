package riftwarp.std
import scala.xml.{ Elem => XmlElem }
import scalaz._
import scalaz.Scalaz._
import scalaz.Tree._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.xml.all._
import riftwarp._
import scala.Array.canBuildFrom
import scala.xml.{ Elem => XmlElem }

object FromStdLibXmlRematerializer extends Rematerializer[XmlElem] {
  override val channel = WarpChannels.`rift-xml-std`
  override def rematerialize(what: XmlElem, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: XmlElem): AlmValidation[WarpPackage] =
    what.label match {
      case "Value" => extractPrimitiveFromElem(what)
      case "Bytes" => extractBytes(what)
      case "Collection" => extractCollection(what)
      case "Assoc" => extractAssoc(what)
      case "Tree" => extractTree(what)
      case "Tuple2" => extractTree(what)
      case "Tuple3" => extractTree(what)
      case "Base64" => extractBase64(what)
      case _ => extractObject(what)
    }

  private def extractObject(what: XmlElem): AlmValidation[WarpObject] =
    for {
      td <- (what \@? "type").map(tstr => WarpDescriptor.parse(tstr)).validationOut
      elems <- what.elems.map(elem =>
        (elem.elems.headOption match {
          case Some(v) => extract(v).map(x => (elem.label, Some(x)))
          case None => (elem.label, None).success
        }).toAgg).toVector.sequence
    } yield WarpObject(td, elems.map(x => WarpElement(x._1, x._2)))

  private def extractTuple2(from: XmlElem): AlmValidation[(WarpPackage, WarpPackage)] =
    for {
      elemA <- from \! "a"
      elemB <- from \! "b"
      va <- elemA.firstChildNode
      vb <- elemB.firstChildNode
      a <- extract(va)
      b <- extract(vb)
    } yield (a, b)

  private def extractTuple3(from: XmlElem): AlmValidation[(WarpPackage, WarpPackage, WarpPackage)] =
    for {
      elemA <- from \! "a"
      elemB <- from \! "b"
      elemC <- from \! "c"
      va <- elemA.firstChildNode
      vb <- elemB.firstChildNode
      vc <- elemC.firstChildNode
      a <- extract(va)
      b <- extract(vb)
      c <- extract(vc)
    } yield (a, b, c)
    
  private def extractCollection(what: XmlElem): AlmValidation[WarpCollection] =
    what.elems.map(elem => extract(elem).toAgg).toVector.sequence.map(WarpCollection(_))

  private def extractAssoc(what: XmlElem): AlmValidation[WarpAssociativeCollection] =
    what.elems.map(elem => extractTuple2(elem).toAgg).toVector.sequence.map(WarpAssociativeCollection(_))

  private def extractTree(what: XmlElem): AlmValidation[WarpTree] =
    what.firstChildNode.flatMap(node => extractTreeNodes(node).map(WarpTree(_)))

  private def extractTreeNodes(from: XmlElem): AlmValidation[Tree[WarpPackage]] = 
    from.label match {
      case "leaf" => from.firstChildNode.flatMap(x => extract(x).map(Tree(_)))
      case "node" => 
        for {
          labelElem <- from \! ("label")
          vLabel <- labelElem.firstChildNode
          subforestElem <- from \! ("subforest")
          label <- extract(vLabel)
          subforest <- subforestElem.elems.map(x => extractTreeNodes(x).toAgg).toList.sequence
        } yield label.node(subforest: _*)
      case x => ParsingProblem(s"$x is not a label for a tree node item").failure
    }
    
  private def extractBytes(what: XmlElem): AlmValidation[WarpBytes] =
    what.text.split(",").map(_.toByteAlm.toAgg).toVector.sequence.map(x => WarpBytes(x.toArray))

  private def extractBase64(what: XmlElem): AlmValidation[WarpBlob] = {
    val str = what.text
    ParseFuns.parseBase64Alm(str).map(x => WarpBlob(x.toVector))
  }
    
  def unescapeString(str: String): String = {
    //scala.xml.Utility.unescape(str, new StringBuilder).result
    str
  }

  private def extractPrimitiveFromElem(value: XmlElem): AlmValidation[WarpPrimitive] =
    (value \@ "type") match {
	    case "String" => extractString(value)
	    case "Boolean" => extractBoolean(value)
	    case "Byte" => extractByte(value)
	    case "Short" => extractShort(value)
	    case "Int" => extractInt(value)
	    case "Long" => extractLong(value)
	    case "BigInt" => extractBigInt(value)
	    case "Float" => extractFloat(value)
	    case "Double" => extractDouble(value)
	    case "BigDecimal" => extractBigDecimal(value)
	    case "DateTime" => extractDateTime(value)
	    case "LocalDateTime" => extractLocalDateTime(value)
	    case "Duration" => extractDuration(value)
	    case "Uuid" => extractUuid(value)
	    case "Uri" => extractUri(value)
	    case x => BadDataProblem(s"Could not extract a primitive from an XmlElem because its attribute 'type' specifies an unknown data type '$x'. The element was: ${value.label}").failure
      }

  private def extractString(value: XmlElem): AlmValidation[WarpPrimitive] = WarpString(unescapeString(value.text)).success
  private def extractBoolean(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractBoolean.map(WarpBoolean(_))
  private def extractByte(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractByte.map(WarpByte(_))
  private def extractShort(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractShort.map(WarpShort(_))
  private def extractInt(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractInt.map(WarpInt(_))
  private def extractLong(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractLong.map(WarpLong(_))
  private def extractBigInt(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractBigInt.map(WarpBigInt(_))
  private def extractFloat(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractFloat.map(WarpFloat(_))
  private def extractDouble(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDouble.map(WarpDouble(_))
  private def extractBigDecimal(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDecimal.map(WarpBigDecimal(_))
  private def extractDateTime(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDateTime.map(WarpDateTime(_))
  private def extractLocalDateTime(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractLocalDateTime.map(WarpLocalDateTime(_))
  private def extractDuration(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractDuration.map(WarpDuration(_))
  private def extractUuid(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractUuid.map(WarpUuid(_))
  private def extractUri(value: XmlElem): AlmValidation[WarpPrimitive] = value.extractUri.map(WarpUri(_))

}