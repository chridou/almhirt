package almhirt.riftwarp.impl.dematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

class ToMapDematerializer(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers) extends DematerializesToMap with NoneHasNoEffectDematerializationFunnel {
  def dematerialize: AlmValidation[DematerializesTo] = state.success

  def addString(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addBoolean(ident: String, aValue: Boolean) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addByte(ident: String, aValue: Byte) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addInt(ident: String, aValue: Int) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addLong(ident: String, aValue: Long) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBigInt(ident: String, aValue: BigInt) = (ToMapDematerializer(state + (ident -> aValue))).success
  
  def addFloat(ident: String, aValue: Float) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addDouble(ident: String, aValue: Double) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBigDecimal(ident: String, aValue: BigDecimal) = (ToMapDematerializer(state + (ident -> aValue))).success
  
  def addByteArray(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addBlob(ident: String, aValue: Array[Byte]) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = (ToMapDematerializer(state + (ident -> aValue))).success
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addJson(ident: String, aValue: String) = (ToMapDematerializer(state + (ident -> aValue))).success
  def addXml(ident: String, aValue: scala.xml.Node) = (ToMapDematerializer(state + (ident -> aValue))).success

  def addComplexRaw(ident: String, aComplexType: AnyRef, clazz: Class[_ <: AnyRef]): AlmValidation[Dematerializer] = sys.error("")
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T, dec: T => Dematerializer)(implicit m: Manifest[T]): AlmValidation[Dematerializer] = sys.error("")
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T)(implicit m: Manifest[T]): AlmValidation[Dematerializer] = sys.error("")
 
  def addTypeDescriptor(descriptor: TypeDescriptor) = (ToMapDematerializer(state + ("typedescriptor" -> descriptor))).success

}

object ToMapDematerializer {
  def apply()(implicit hasDecomposers: HasDecomposers): ToMapDematerializer = apply(Map.empty)
  def apply(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers): ToMapDematerializer = new ToMapDematerializer(state)
}