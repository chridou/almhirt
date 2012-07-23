package almhirt.xtractnduce

import org.joda.time.DateTime

sealed trait NDuceScript

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
case class SetElement(key: String, value: NDuceElem) extends NDuceScriptOp
case class SetElementOpt(key: String, value: Option[NDuceElem]) extends NDuceScriptOp
case class SetElements(key: String, elements: Seq[NDuceElem]) extends NDuceScriptOp
case class SetPrimitives(key: String, primitives: Seq[Any]) extends NDuceScriptOp
case class NDuceElem(key: String, values: Seq[NDuceScriptOp]) extends NDuceScriptOp with NDuceScript with NDuceScribe {
  def setString(key: String, value: String) = copy(values = values :+ SetString(key, value))
  def setString(key: String, value: Option[String]) = copy(values = values :+ SetStringOpt(key, value))
  def setInt(key: String, value: Int) = copy(values = values :+ SetInt(key, value))
  def setInt(key: String, value: Option[Int]) = copy(values = values :+ SetIntOpt(key, value))
  def setLong(key: String, value: Long) = copy(values = values :+ SetLong(key, value))
  def setLong(key: String, value: Option[Long]) = copy(values = values :+ SetLongOpt(key, value))
  def setDouble(key: String, value: Double) = copy(values = values :+ SetDouble(key, value))
  def setDouble(key: String, value: Option[Double]) = copy(values = values :+ SetDoubleOpt(key, value))
  def setFloat(key: String, value: Float) = copy(values = values :+ SetFloat(key, value))
  def setFloat(key: String, value: Option[Float]) = copy(values = values :+ SetFloatOpt(key, value))
  def setBoolean(key: String, value: Boolean) = copy(values = values :+ SetBoolean(key, value))
  def setBoolean(key: String, value: Option[Boolean]) = copy(values = values :+ SetBooleanOpt(key, value))
  def setDecimal(key: String, value: BigDecimal) = copy(values = values :+ SetDecimal(key, value))
  def setDecimal(key: String, value: Option[BigDecimal]) = copy(values = values :+ SetDecimalOpt(key, value))
  def setDateTime(key: String, value: DateTime) = copy(values = values :+ SetDateTime(key, value))
  def setDateTime(key: String, value: Option[DateTime]) = copy(values = values :+ SetDateTimeOpt(key, value))
  def setBytes(key: String, value: Array[Byte]) = copy(values = values :+ SetBytes(key, value))
  def setBytes(key: String, value: Option[Array[Byte]]) = copy(values = values :+ SetBytesOpt(key, value))
  
  def setElement(key: String, element: NDuceElem) = copy(values = values :+ SetElement(key, element))
  def setElement(key: String, element: Option[NDuceElem]) = copy(values = values :+ SetElementOpt(key, element))
  def setElements(key: String, elements: NDuceElem*) = copy(values = values :+ SetElements(key, elements))
  def setPrimitives(key: String, primitives: Any*) = copy(values = values :+ SetPrimitives(key, primitives))
}


