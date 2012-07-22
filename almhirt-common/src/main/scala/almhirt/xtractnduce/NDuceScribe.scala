package almhirt.xtractnduce

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
  def setElements(key: String, elements: NDuceElem*): NDuceScript
  def setPrimitives(key: String, primitives: Any*): NDuceScript
}

object NDuceScribe {
  def scribble(name: String) = NDuceElem(name, Seq.empty)
  def scribble(name: String, children: NDuceScript*) = NDuceElem(name, children)
}