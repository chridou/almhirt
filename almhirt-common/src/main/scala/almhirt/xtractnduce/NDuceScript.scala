package almhirt.xtractnduce

import java.util.UUID
import org.joda.time.DateTime

sealed trait NDuceScript extends NDuceScribe {
  def name: String
  def ops: Seq[NDuceScriptOp]
  def nameIsTypeInfo(): NDuceScript
  def typeInfo: Option[String]
  def xtract(): NDuceXTractor = new NDuceXTractor(this)
}

sealed trait NDuceScriptOp {
  def key: String
}

case class NDuceString(key: String, value: String) extends NDuceScriptOp
case class NDuceStringOpt(key: String, value: Option[String]) extends NDuceScriptOp
case class NDuceInt(key: String, value: Int) extends NDuceScriptOp
case class NDuceIntOpt(key: String, value: Option[Int]) extends NDuceScriptOp
case class NDuceLong(key: String, value: Long) extends NDuceScriptOp
case class NDuceLongOpt(key: String, value: Option[Long]) extends NDuceScriptOp
case class NDuceDouble(key: String, value: Double) extends NDuceScriptOp
case class NDuceDoubleOpt(key: String, value: Option[Double]) extends NDuceScriptOp
case class NDuceFloat(key: String, value: Float) extends NDuceScriptOp
case class NDuceFloatOpt(key: String, value: Option[Float]) extends NDuceScriptOp
case class NDuceBoolean(key: String, value: Boolean) extends NDuceScriptOp
case class NDuceBooleanOpt(key: String, value: Option[Boolean]) extends NDuceScriptOp
case class NDuceDecimal(key: String, value: BigDecimal) extends NDuceScriptOp
case class NDuceDecimalOpt(key: String, value: Option[BigDecimal]) extends NDuceScriptOp
case class NDuceDateTime(key: String, value: DateTime) extends NDuceScriptOp
case class NDuceDateTimeOpt(key: String, value: Option[DateTime]) extends NDuceScriptOp
case class NDuceUUID(key: String, value: UUID) extends NDuceScriptOp
case class NDuceUUIDOpt(key: String, value: Option[UUID]) extends NDuceScriptOp
case class NDuceBytes(key: String, value: Array[Byte]) extends NDuceScriptOp
case class NDuceBytesOpt(key: String, value: Option[Array[Byte]]) extends NDuceScriptOp
case class NDuceElement(key: String, scriptElement: NDuceScript) extends NDuceScriptOp
case class NDuceElementOpt(key: String, scriptElement: Option[NDuceScript]) extends NDuceScriptOp
case class NDuceElements(key: String, scriptElements: Seq[NDuceScript]) extends NDuceScriptOp
case class NDucePrimitives(key: String, primitives: Seq[Any]) extends NDuceScriptOp
case class NDuceAggregate(val name: String, val ops: Seq[NDuceScriptOp], val typeInfo: Option[String] = None) extends NDuceScriptOp with NDuceScript {
  val key = name
  
  def setString(key: String, value: String) = copy(ops = ops :+ NDuceString(key, value))
  def setString(key: String, value: Option[String]) = copy(ops = ops :+ NDuceStringOpt(key, value))
  def setInt(key: String, value: Int) = copy(ops = ops :+ NDuceInt(key, value))
  def setInt(key: String, value: Option[Int]) = copy(ops = ops :+ NDuceIntOpt(key, value))
  def setLong(key: String, value: Long) = copy(ops = ops :+ NDuceLong(key, value))
  def setLong(key: String, value: Option[Long]) = copy(ops = ops :+ NDuceLongOpt(key, value))
  def setDouble(key: String, value: Double) = copy(ops = ops :+ NDuceDouble(key, value))
  def setDouble(key: String, value: Option[Double]) = copy(ops = ops :+ NDuceDoubleOpt(key, value))
  def setFloat(key: String, value: Float) = copy(ops = ops :+ NDuceFloat(key, value))
  def setFloat(key: String, value: Option[Float]) = copy(ops = ops :+ NDuceFloatOpt(key, value))
  def setBoolean(key: String, value: Boolean) = copy(ops = ops :+ NDuceBoolean(key, value))
  def setBoolean(key: String, value: Option[Boolean]) = copy(ops = ops :+ NDuceBooleanOpt(key, value))
  def setDecimal(key: String, value: BigDecimal) = copy(ops = ops :+ NDuceDecimal(key, value))
  def setDecimal(key: String, value: Option[BigDecimal]) = copy(ops = ops :+ NDuceDecimalOpt(key, value))
  def setDateTime(key: String, value: DateTime) = copy(ops = ops :+ NDuceDateTime(key, value))
  def setDateTime(key: String, value: Option[DateTime]) = copy(ops = ops :+ NDuceDateTimeOpt(key, value))
  def setUUID(key: String, value: java.util.UUID) = copy(ops = ops :+ NDuceUUID(key, value))
  def setUUID(key: String, value: Option[java.util.UUID]) = copy(ops = ops :+ NDuceUUIDOpt(key, value))
  def setBytes(key: String, value: Array[Byte]) = copy(ops = ops :+ NDuceBytes(key, value))
  def setBytes(key: String, value: Option[Array[Byte]]) = copy(ops = ops :+ NDuceBytesOpt(key, value))
  
  def setElement(key: String, scriptElement: NDuceScript) = copy(ops = ops :+ NDuceElement(key, scriptElement))
  def setElement(key: String, scriptElement: Option[NDuceScript]) = copy(ops = ops :+ NDuceElementOpt(key, scriptElement))
  def setElements(key: String, scriptElements: NDuceScript*) = copy(ops = ops :+ NDuceElements(key, scriptElements))
  def setPrimitives(key: String, primitives: Any*) = copy(ops = ops :+ NDucePrimitives(key, primitives))
  
  def nameIsTypeInfo() = copy(typeInfo = Some(name))
}


