package almhirt.nduce

import scalaz._
import Scalaz._
import org.joda.time.DateTime

trait NDuceScribe {
  def setString(key: String, value: String): NDuceScript
  def setInt(key: String, value: Int): NDuceScript
  def setLong(key: String, value: Long): NDuceScript
  def setDouble(key: String, value: Double): NDuceScript
  def setFloat(key: String, value: Float): NDuceScript
  def setBoolean(key: String, value: Boolean): NDuceScript
  def setDecimal(key: String, value: BigDecimal): NDuceScript
  def setDateTime(key: String, value: DateTime): NDuceScript
  def setBytes(key: String, value: Array[Byte]): NDuceScript
  
  def setElement(key: String, element: NDuceElem): NDuceScript
  def setElements(key: String, elements: TraversableOnce[NDuceElem]): NDuceScript
  def setPrimitives(key: String, primitives: TraversableOnce[Any]): NDuceScript
}

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
case class SetElement(key: String, element: NDuceElem) extends NDuceScript
case class SetElements(key: String, elements: TraversableOnce[NDuceElem]) extends NDuceScript
case class SetPrimitives(key: String, primitives: TraversableOnce[Any]) extends NDuceScript
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
  
  def setElement(key: String, element: NDuceElem) = copy(values = values :+ SetElement(key, element))
  def setElements(key: String, elements: TraversableOnce[NDuceElem]) = copy(values = values :+ SetElements(key, elements))
  def setPrimitives(key: String, primitives: TraversableOnce[Any]) = copy(values = values :+ SetPrimitives(key, primitives))
}

object NDuceScribe {
  def scribble(name: String) = NDuceElem(name, Seq.empty)
}
