package almhirt.riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

class ImmutableToMapDematerializer(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers) extends DematerializesToMap with NoneHasNoEffectDematerializationFunnel {
  def dematerialize: AlmValidation[DematerializesTo] = state.success

  def addString(ident: String, aValue: String) = ImmutableToMapDematerializer(state + (ident -> aValue)).success

  def addBoolean(ident: String, aValue: Boolean) = ImmutableToMapDematerializer(state + (ident -> aValue)).success

  def addByte(ident: String, aValue: Byte) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addInt(ident: String, aValue: Int) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addLong(ident: String, aValue: Long) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addBigInt(ident: String, aValue: BigInt) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  
  def addFloat(ident: String, aValue: Float) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addDouble(ident: String, aValue: Double) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addBigDecimal(ident: String, aValue: BigDecimal) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  
  def addByteArray(ident: String, aValue: Array[Byte]) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addBlob(ident: String, aValue: Array[Byte]) = ImmutableToMapDematerializer(state + (ident -> aValue)).success

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID) = ImmutableToMapDematerializer(state + (ident -> aValue)).success

  def addJson(ident: String, aValue: String) = ImmutableToMapDematerializer(state + (ident -> aValue)).success
  def addXml(ident: String, aValue: scala.xml.Node) = ImmutableToMapDematerializer(state + (ident -> aValue)).success

  def addComplexRaw(ident: String, aComplexType: AnyRef, clazz: Class[_ <: AnyRef]): AlmValidation[Dematerializer] = sys.error("")
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T, dec: T => Dematerializer)(implicit m: Manifest[T]): AlmValidation[Dematerializer] = sys.error("")
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T)(implicit m: Manifest[T]): AlmValidation[Dematerializer] = sys.error("")
 
  def addTypeDescriptor(typeName: String) = ImmutableToMapDematerializer(state + ("typedescriptor" -> typeName)).success

}

object ImmutableToMapDematerializer {
  def apply()(implicit hasDecomposers: HasDecomposers): ImmutableToMapDematerializer = apply(Map.empty)
  def apply(state: Map[String, Any])(implicit hasDecomposers: HasDecomposers): ImmutableToMapDematerializer = new ImmutableToMapDematerializer(state)
}