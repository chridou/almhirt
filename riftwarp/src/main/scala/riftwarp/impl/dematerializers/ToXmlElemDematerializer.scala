package riftwarp.impl.warpSequencers

import java.util.{UUID => JUUID}
import scala.reflect.ClassTag
import scala.annotation.tailrec
import scala.xml.{ Elem => XmlElem, NodeSeq }
import scalaz._, Scalaz._
import org.joda.time.DateTime
import almhirt.common._
import riftwarp._
import riftwarp.impl.dematerializers.DematerializerTemplate

object ToXmlElemDematerializerFuns {
  val mapString = (value: String) => <v type="String">{scala.xml.Utility.escape(value)}</v>
  val mapBoolean = (value: Boolean) => <v type="Boolean">{value.toString}</v>
  val mapByte = (value: Byte) => <v type="Byte">{value.toString}</v>
  val mapInt = (value: Int) => <v type="Int">{value.toString}</v>
  val mapLong = (value: Long) => <v type="Long">{value.toString}</v>
  val mapBigInt = (value: BigInt) => <v type="BigInt">{value.toString}</v>
  val mapFloat = (value: Float) => <v type="Float">{value.toString}</v>
  val mapDouble = (value: Double) => <v type="Double">{value.toString}</v>
  val mapBigDecimal = (value: BigDecimal) => <v type="BigDecimal">{value.toString}</v>
  val mapDateTime = (value: DateTime) => <v type="DateTime">{value.toString()}</v>
  val mapUuid = (value: JUUID) => <v type="Uuid">{value.toString}</v>
  val mapUri = (value: java.net.URI) => <v type="Uri">{scala.xml.Utility.escape(value.toString)}</v>

  def valueMapperByType[A](implicit m: ClassTag[A]): AlmValidation[A => XmlElem] = {
    val t = m.runtimeClass
    if (t == classOf[String])
      (mapString).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Boolean])
      (mapBoolean).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Byte])
      (mapByte).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Int])
      (mapInt).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Long])
      (mapLong).asInstanceOf[A => XmlElem].success
    else if (t == classOf[BigInt])
      (mapBigInt).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Float])
      (mapFloat).asInstanceOf[A => XmlElem].success
    else if (t == classOf[Double])
      (mapDouble).asInstanceOf[A => XmlElem].success
    else if (t == classOf[BigDecimal])
      (mapBigDecimal).asInstanceOf[A => XmlElem].success
    else if (t == classOf[DateTime])
      (mapDateTime).asInstanceOf[A => XmlElem].success
    else if (t == classOf[_root_.java.util.UUID])
      (mapUuid).asInstanceOf[A => XmlElem].success
    else if (t == classOf[java.net.URI])
      (mapUri).asInstanceOf[A => XmlElem].success
    else
      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
  }

  def valueMapperForAny(lookupFor: Any): AlmValidation[Any => XmlElem] = {
    if (lookupFor.isInstanceOf[String])
      valueMapperByType[String].map(mapper => (x: Any) => {mapper(x.asInstanceOf[String])})
    else if (lookupFor.isInstanceOf[Boolean])
      valueMapperByType[Boolean].map(mapper => (x: Any) => mapper(x.asInstanceOf[Boolean]))
    else if (lookupFor.isInstanceOf[Byte])
      valueMapperByType[Byte].map(mapper => (x: Any) => mapper(x.asInstanceOf[Byte]))
    else if (lookupFor.isInstanceOf[Int])
      valueMapperByType[Int].map(mapper => (x: Any) => mapper(x.asInstanceOf[Int]))
    else if (lookupFor.isInstanceOf[Long])
      valueMapperByType[Long].map(mapper => (x: Any) => mapper(x.asInstanceOf[Long]))
    else if (lookupFor.isInstanceOf[BigInt])
      valueMapperByType[BigInt].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigInt]))
    else if (lookupFor.isInstanceOf[Float])
      valueMapperByType[Float].map(mapper => (x: Any) => mapper(x.asInstanceOf[Float]))
    else if (lookupFor.isInstanceOf[Double])
      valueMapperByType[Double].map(mapper => (x: Any) => mapper(x.asInstanceOf[Double]))
    else if (lookupFor.isInstanceOf[BigDecimal])
      valueMapperByType[BigDecimal].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigDecimal]))
    else if (lookupFor.isInstanceOf[DateTime])
      valueMapperByType[DateTime].map(mapper => (x: Any) => mapper(x.asInstanceOf[DateTime]))
    else if (lookupFor.isInstanceOf[_root_.java.util.UUID])
      valueMapperByType[_root_.java.util.UUID].map(mapper => (x: Any) => mapper(x.asInstanceOf[_root_.java.util.UUID]))
    else if (lookupFor.isInstanceOf[java.net.URI])
      valueMapperByType[java.net.URI].map(mapper => (x: Any) => mapper(x.asInstanceOf[java.net.URI]))
    else
      UnspecifiedProblem("No primitive mapper found for %s".format(lookupFor.getClass.getName())).failure
  }

  
  @tailrec
  private def createInnerXml(rest: List[XmlElem], acc: NodeSeq): XmlElem =
    rest match {
      case Nil => <items>{acc}</items>
      case h :: t => createInnerXml(t, acc ++ h)
    }

  def foldParts(items: List[XmlElem]): XmlElem = createInnerXml(items, NodeSeq.Empty) 

  def foldTree(tree: scalaz.Tree[XmlElem]): XmlElem = 
    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTree).toList) :: Nil)
     
}

object ToXmlElemDematerializer extends DematerializerTemplate[DimensionXmlElem]{
  import ToXmlElemDematerializerFuns._
  protected override def valueReprToDim(repr: ValueRepr): DimensionXmlElem = DimensionXmlElem(repr)
  protected override def dimToReprValue(dim: DimensionXmlElem): XmlElem = dim.manifestation
  protected override def foldReprs(elems: Iterable[XmlElem]): XmlElem = foldParts(elems.toList)
  protected override def getPrimitiveToRepr[A](implicit tag: ClassTag[A]): AlmValidation[(A => XmlElem)] = valueMapperByType[A]
  protected override def getAnyPrimitiveToRepr(what: Any): AlmValidation[(Any => XmlElem)] = valueMapperForAny(what)
  protected override def getTreeRepr(tree: scalaz.Tree[XmlElem]): ValueRepr = foldTree(tree)
  
  override def getStringRepr(aValue: String) = mapString(aValue)
  override def getBooleanRepr(aValue: Boolean) = mapBoolean(aValue)
  override def getByteRepr(aValue: Byte) = mapByte(aValue)
  override def getIntRepr(aValue: Int) = mapInt(aValue)
  override def getLongRepr(aValue: Long) = mapLong(aValue)
  override def getBigIntRepr(aValue: BigInt) = mapBigInt(aValue)
  override def getFloatRepr(aValue: Float) = mapFloat(aValue)
  override def getDoubleRepr(aValue: Double) = mapDouble(aValue)
  override def getBigDecimalRepr(aValue: BigDecimal) = mapBigDecimal(aValue)
  override def getByteArrayRepr(aValue: Array[Byte]) = <bytes>{aValue.mkString(",")}</bytes>
  override def getBase64EncodedByteArrayRepr(aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    mapString(base64)
  }
  override def getByteArrayBlobEncodedRepr(aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    mapString(theBlob)
  }
  override def getDateTimeRepr(aValue: org.joda.time.DateTime) = mapDateTime(aValue)
  override def getUriRepr(aValue: _root_.java.net.URI) = mapUri(aValue)
  override def getUuidRepr(aValue: _root_.java.util.UUID) = mapUuid(aValue)

  
}