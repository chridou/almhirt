package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

/** Pushes atoms into the void */
trait DematerializationFunnel { self: Dematerializer => 
  def addString(ident: String, aValue: String): AlmValidation[Dematerializer]
  def addString(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer]
  def addBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[Dematerializer]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer]
  def addByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[Dematerializer]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer]
  def addInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[Dematerializer]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer]
  def addLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[Dematerializer]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer]
  def addBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[Dematerializer]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer]
  def addFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[Dematerializer]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer]
  def addDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[Dematerializer]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer]
  def addBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[Dematerializer]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer]
  def addByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer]
  def addBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer]
  def addDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer]
  def addUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[Dematerializer]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer]
  def addJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer]
  def addXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer]

  def addComplexRaw(ident: String, aComplexType: AnyRef, clazz: Class[_ <: AnyRef]): AlmValidation[Dematerializer]
  def addOptionalComplexRaw(ident: String, anOptionalComplexType: Option[_ <: AnyRef], clazz: Class[_ <: AnyRef]): AlmValidation[Dematerializer]
    
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T, dec: T => Dematerializer)(implicit m: Manifest[T]): AlmValidation[Dematerializer]
  def addComplexType[T <: AnyRef](ident: String, anOptionalComplexType: Option[T], dec: T => Dematerializer)(implicit m: Manifest[T]): AlmValidation[Dematerializer]
  def addComplexType[T <: AnyRef](ident: String, aComplexType: T)(implicit m: Manifest[T]): AlmValidation[Dematerializer]
  def addComplexType[T <: AnyRef](ident: String, anOptionalComplexType: Option[T])(implicit m: Manifest[T]): AlmValidation[Dematerializer]
  
  def addTypeDescriptor(typeName: String): AlmValidation[Dematerializer]
   
  def fail(prob: Problem): AlmValidation[Dematerializer] = prob.failure
  
//  def flatMap(f: DematerializationFunnel => AlmValidation[DematerializationFunnel]): AlmValidation[DematerializationFunnel] =
//    f(this).fold(
//        fail => fail.failure, 
//        succ => succ.success)
//  def map(f: DematerializationFunnel => DematerializationFunnel): AlmValidation[DematerializationFunnel] =
//    f(this).success
}

trait NoneHasNoEffectDematerializationFunnel extends DematerializationFunnel { self: Dematerializer =>
  def addString(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addString(ident, _), this.success)

  def addBoolean(ident: String, anOptionalValue: Option[Boolean]) = option.cata(anOptionalValue)(addBoolean(ident, _), this.success)

  def addByte(ident: String, anOptionalValue: Option[Byte]) = option.cata(anOptionalValue)(addByte(ident, _), this.success)
  def addInt(ident: String, anOptionalValue: Option[Int]) = option.cata(anOptionalValue)(addInt(ident, _), this.success)
  def addLong(ident: String, anOptionalValue: Option[Long]) = option.cata(anOptionalValue)(addLong(ident, _), this.success)
  def addBigInt(ident: String, anOptionalValue: Option[BigInt]) = option.cata(anOptionalValue)(addBigInt(ident, _), this.success)
  
  def addFloat(ident: String, anOptionalValue: Option[Float]) = option.cata(anOptionalValue)(addFloat(ident, _), this.success)
  def addDouble(ident: String, anOptionalValue: Option[Double]) = option.cata(anOptionalValue)(addDouble(ident, _), this.success)
  def addBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = option.cata(anOptionalValue)(addBigDecimal(ident, _), this.success)
  
  def addByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArray(ident, _), this.success)
  def addBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addBlob(ident, _), this.success)

  def addDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = option.cata(anOptionalValue)(addDateTime(ident, _), this.success)
  
  def addUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = option.cata(anOptionalValue)(addUuid(ident, _), this.success)

  def addJson(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addJson(ident, _), this.success)
  def addXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = option.cata(anOptionalValue)(addXml(ident, _), this.success)

  def addOptionalComplexRaw(ident: String, anOptionalComplexType: Option[_ <: AnyRef], clazz: Class[_ <: AnyRef]): AlmValidation[Dematerializer] =
     option.cata(anOptionalComplexType)(addComplexRaw(ident, _, clazz), this.success)
  
  def addComplexType[T <: AnyRef](ident: String, anOptionalComplexType: Option[T], dec: T => Dematerializer)(implicit m: Manifest[T]): AlmValidation[Dematerializer] =
    option.cata(anOptionalComplexType)(addComplexType(ident, _, dec), this.success)
    
  def addComplexType[T <: AnyRef](ident: String, anOptionalComplexType: Option[T])(implicit m: Manifest[T]): AlmValidation[Dematerializer] =
    option.cata(anOptionalComplexType)(addComplexType(ident, _), this.success)
    
}