//package almhirt.riftwarp.impl
//
//import scalaz.syntax.validation._
//import almhirt.riftwarp._
//
//trait DevNullDematerializer extends Dematerializer with DematerializationFunnel {
//  def addString(ident: String, aValue: String) = this.success
//  def addString(ident: String, anOptionalValue: Option[String]) = this.success
//
//  def addBoolean(ident: String, aValue: String) = this.success
//  def addBoolean(ident: String, anOptionalValue: Option[String]) = this.success
//
//  def addByte(ident: String, aValue: Byte) = this.success
//  def addByte(ident: String, anOptionalValue: Option[Byte]) = this.success
//  def addInt(ident: String, aValue: Int) = this.success
//  def addInt(ident: String, anOptionalValue: Option[Int]) = this.success
//  def addLong(ident: String, aValue: Long) = this.success
//  def addLong(ident: String, anOptionalValue: Option[Long]) = this.success
//  def addBigInt(ident: String, aValue: BigInt) = this.success
//  def addBigInt(ident: String, anOptionalValue: Option[BigInt]) = this.success
//  
//  def addFloat(ident: String, aValue: Float) = this.success
//  def addFloat(ident: String, anOptionalValue: Option[Float]) = this.success
//  def addDouble(ident: String, aValue: Double) = this.success
//  def addDouble(ident: String, anOptionalValue: Option[Double]) = this.success
//  def addBigDecimal(ident: String, aValue: BigDecimal) = this.success
//  def addBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = this.success
//  
//  def addByteArray(ident: String, aValue: Array[Byte]) = this.success
//  def addByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = this.success
//  def addBlob(ident: String, aValue: Array[Byte]) = this.success
//  def addBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = this.success
//
//  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = this.success
//  def addDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = this.success
//  
//  def addUuid(ident: String, aValue: java.util.UUID) = this.success
//  def addUuid(ident: String, anOptionalValue: Option[java.util.UUID]) = this.success
//
//  def addJson(ident: String, aValue: String) = this.success
//  def addJson(ident: String, anOptionalValue: Option[String]) = this.success
//  def addXml(ident: String, aValue: scala.xml.Node) = this.success
//  def addXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = this.success
//
//  def addComplexType[T](ident: String, aComplexType: T, dec: Decomposer[T]) = this.success
//  def addComplexType[T](ident: String, anOptionalComplexType: Option[T], dec: Decomposer[T]) = this.success
//  
//  def addTypeDescriptor(typeName: String) = this.success
//  
//}