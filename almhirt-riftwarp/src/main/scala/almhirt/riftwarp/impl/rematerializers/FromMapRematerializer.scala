package almhirt.riftwarp.impl.rematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.riftwarp._

class FromMapRematerializationArray(theMap: Map[String, Any]) extends RematiarializationArrayBasedOnOptionGetters{
  def tryGetString(ident: String) = option.cata(theMap.get(ident))(almCast(_).map(Some(_)), None.success)

//  def getBoolean(ident: String): AlmValidationSBD[String]
//  def tryGetBoolean(ident: String): AlmValidationSBD[Option[String]]

  def tryGetByte(ident: String) = option.cata(theMap.get(ident))(almCast(_).map(Some(_)), None.success)
  def tryGetInt(ident: String) = option.cata(theMap.get(ident))(almCast(_).map(Some(_)), None.success)
//  def tryGetLong(ident: String): AlmValidationSBD[Option[Long]]
//  def tryGetBigInt(ident: String): AlmValidationSBD[Option[BigInt]]
//  
//  def tryGetFloat(ident: String): AlmValidationSBD[Option[Float]]
//  def tryGetDouble(ident: String): AlmValidationSBD[Option[Double]]
//  def tryGetBigDecimal(ident: String): AlmValidationSBD[Option[BigDecimal]]
//  
//  def tryGetByteArray(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//  def tryGetBlob(ident: String): AlmValidationSBD[Option[Array[Byte]]]
//
//  def tryGetDateTime(ident: String): AlmValidationSBD[Option[org.joda.time.DateTime]]
//  
//  def tryGetUuid(ident: String): AlmValidationSBD[Option[java.util.UUID]]
//
//  def tryGetJson(ident: String): AlmValidationSBD[Option[String]]
//  def tryGetXml(ident: String): AlmValidationSBD[Option[scala.xml.Node]]

  def getTypeDescriptor = tryGetFromMap(TypeDescriptor.defaultKey, theMap, Major).bind(almCast(_))
  
}

object FromMapRematerializationArray extends FromMapRematerializationArrayFactory {
  def apply(): FromMapRematerializationArray = apply(Map.empty)
  def apply(state: Map[String, Any]): FromMapRematerializationArray = new FromMapRematerializationArray(state)
  def createRematerializationArray(from: Map[String, Any]): RematerializationArray = apply(from)
}