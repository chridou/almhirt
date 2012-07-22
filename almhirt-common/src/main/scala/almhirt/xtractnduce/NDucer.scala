package almhirt.xtractnduce

import org.joda.time.DateTime


sealed trait NDuceScript
case class SetString(key: String, value: String) extends NDuceScript
case class SetInt(key: String, value: Int) extends NDuceScript
case class SetLong(key: String, value: Long) extends NDuceScript
case class SetDouble(key: String, value: Double) extends NDuceScript
case class SetFloat(key: String, value: Float) extends NDuceScript
case class SetBoolean(key: String, value: Boolean) extends NDuceScript
case class SetDecimal(key: String, value: BigDecimal) extends NDuceScript
case class SetDateTime(key: String, value: DateTime) extends NDuceScript
case class SetBytes(key: String, value: Array[Byte]) extends NDuceScript
case class SetElements(key: String, elements: Seq[NDuceElem]) extends NDuceScript
case class SetPrimitives(key: String, primitives: Seq[Any]) extends NDuceScript
case class NDuceElem(key: String, values: Seq[NDuceScript]) extends NDuceScript with NDuceScribe {
  def setString(key: String, value: String) = copy(values = values :+ SetString(key, value))
  def setInt(key: String, value: Int) = copy(values = values :+ SetInt(key, value))
  def setLong(key: String, value: Long) = copy(values = values :+ SetLong(key, value))
  def setDouble(key: String, value: Double) = copy(values = values :+ SetDouble(key, value))
  def setFloat(key: String, value: Float) = copy(values = values :+ SetFloat(key, value))
  def setBoolean(key: String, value: Boolean) = copy(values = values :+ SetBoolean(key, value))
  def setDecimal(key: String, value: BigDecimal) = copy(values = values :+ SetDecimal(key, value))
  def setDateTime(key: String, value: DateTime) = copy(values = values :+ SetDateTime(key, value))
  def setBytes(key: String, value: Array[Byte]) = copy(values = values :+ SetBytes(key, value))
  
  def setElement(key: String, element: NDuceElem) = copy(values = values :+ element)
  def setElements(key: String, elements: NDuceElem*) = copy(values = values :+ SetElements(key, elements))
  def setPrimitives(key: String, primitives: Any*) = copy(values = values :+ SetPrimitives(key, primitives))
}


