package almhirt.xtractnduce

import org.joda.time.DateTime

sealed trait NDuceScript {
  def name: String
  def ops: Seq[NDuceScriptOp]
}

sealed trait NDuceScriptOp
case class SetString(key: String, value: String) extends NDuceScriptOp
case class SetStringOpt(key: String, value: Option[String]) extends NDuceScriptOp
case class SetInt(key: String, value: Int) extends NDuceScriptOp
case class SetIntOpt(key: String, value: Option[Int]) extends NDuceScriptOp
case class SetLong(key: String, value: Long) extends NDuceScriptOp
case class SetLongOpt(key: String, value: Option[Long]) extends NDuceScriptOp
case class SetDouble(key: String, value: Double) extends NDuceScriptOp
case class SetDoubleOpt(key: String, value: Option[Double]) extends NDuceScriptOp
case class SetFloat(key: String, value: Float) extends NDuceScriptOp
case class SetFloatOpt(key: String, value: Option[Float]) extends NDuceScriptOp
case class SetBoolean(key: String, value: Boolean) extends NDuceScriptOp
case class SetBooleanOpt(key: String, value: Option[Boolean]) extends NDuceScriptOp
case class SetDecimal(key: String, value: BigDecimal) extends NDuceScriptOp
case class SetDecimalOpt(key: String, value: Option[BigDecimal]) extends NDuceScriptOp
case class SetDateTime(key: String, value: DateTime) extends NDuceScriptOp
case class SetDateTimeOpt(key: String, value: Option[DateTime]) extends NDuceScriptOp
case class SetBytes(key: String, value: Array[Byte]) extends NDuceScriptOp
case class SetBytesOpt(key: String, value: Option[Array[Byte]]) extends NDuceScriptOp
case class SetElement(key: String, scriptElement: NDuceScript) extends NDuceScriptOp
case class SetElementOpt(key: String, scriptElement: Option[NDuceScript]) extends NDuceScriptOp
case class SetElements(key: String, scriptElements: Seq[NDuceScript]) extends NDuceScriptOp
case class SetPrimitives(key: String, primitives: Seq[Any]) extends NDuceScriptOp
case class NDuceElem(val name: String, val ops: Seq[NDuceScriptOp]) extends NDuceScriptOp with NDuceScript with NDuceScribe {
  def setString(key: String, value: String) = copy(ops = ops :+ SetString(key, value))
  def setString(key: String, value: Option[String]) = copy(ops = ops :+ SetStringOpt(key, value))
  def setInt(key: String, value: Int) = copy(ops = ops :+ SetInt(key, value))
  def setInt(key: String, value: Option[Int]) = copy(ops = ops :+ SetIntOpt(key, value))
  def setLong(key: String, value: Long) = copy(ops = ops :+ SetLong(key, value))
  def setLong(key: String, value: Option[Long]) = copy(ops = ops :+ SetLongOpt(key, value))
  def setDouble(key: String, value: Double) = copy(ops = ops :+ SetDouble(key, value))
  def setDouble(key: String, value: Option[Double]) = copy(ops = ops :+ SetDoubleOpt(key, value))
  def setFloat(key: String, value: Float) = copy(ops = ops :+ SetFloat(key, value))
  def setFloat(key: String, value: Option[Float]) = copy(ops = ops :+ SetFloatOpt(key, value))
  def setBoolean(key: String, value: Boolean) = copy(ops = ops :+ SetBoolean(key, value))
  def setBoolean(key: String, value: Option[Boolean]) = copy(ops = ops :+ SetBooleanOpt(key, value))
  def setDecimal(key: String, value: BigDecimal) = copy(ops = ops :+ SetDecimal(key, value))
  def setDecimal(key: String, value: Option[BigDecimal]) = copy(ops = ops :+ SetDecimalOpt(key, value))
  def setDateTime(key: String, value: DateTime) = copy(ops = ops :+ SetDateTime(key, value))
  def setDateTime(key: String, value: Option[DateTime]) = copy(ops = ops :+ SetDateTimeOpt(key, value))
  def setBytes(key: String, value: Array[Byte]) = copy(ops = ops :+ SetBytes(key, value))
  def setBytes(key: String, value: Option[Array[Byte]]) = copy(ops = ops :+ SetBytesOpt(key, value))
  
  def setElement(key: String, scriptElement: NDuceScript) = copy(ops = ops :+ SetElement(key, scriptElement))
  def setElement(key: String, scriptElement: Option[NDuceScript]) = copy(ops = ops :+ SetElementOpt(key, scriptElement))
  def setElements(key: String, scriptElements: NDuceScript*) = copy(ops = ops :+ SetElements(key, scriptElements))
  def setPrimitives(key: String, primitives: Any*) = copy(ops = ops :+ SetPrimitives(key, primitives))
}


