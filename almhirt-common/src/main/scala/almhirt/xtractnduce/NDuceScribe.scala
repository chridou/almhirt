package almhirt.xtractnduce

import org.joda.time.DateTime

trait NDuceScribe {
  def setString(key: String, value: String): NDuceScript
  def setString(key: String, value: Option[String]): NDuceScript
  def setInt(key: String, value: Int): NDuceScript
  def setInt(key: String, value: Option[Int]): NDuceScript
  def setLong(key: String, value: Long): NDuceScript
  def setLong(key: String, value: Option[Long]): NDuceScript
  def setDouble(key: String, value: Double): NDuceScript
  def setDouble(key: String, value: Option[Double]): NDuceScript
  def setFloat(key: String, value: Float): NDuceScript
  def setFloat(key: String, value: Option[Float]): NDuceScript
  def setBoolean(key: String, value: Boolean): NDuceScript
  def setBoolean(key: String, value: Option[Boolean]): NDuceScript
  def setDecimal(key: String, value: BigDecimal): NDuceScript
  def setDecimal(key: String, value: Option[BigDecimal]): NDuceScript
  def setDateTime(key: String, value: DateTime): NDuceScript
  def setDateTime(key: String, value: Option[DateTime]): NDuceScript
  def setBytes(key: String, value: Array[Byte]): NDuceScript
  def setBytes(key: String, value: Option[Array[Byte]]): NDuceScript
  
  def setElement(key: String, scriptElement: NDuceScript): NDuceScript
  def setElement(key: String, scriptElement: Option[NDuceScript]): NDuceScript
  def setElements(key: String, scriptElements: NDuceScript*): NDuceScript
  def setPrimitives(key: String, primitives: Any*): NDuceScript
}

object NDuceScribe {
  def scribble(name: String) = NDuceAggregate(name, Seq.empty)
  def scribble(name: String, children: NDuceScriptOp*) = NDuceAggregate(name, children)
}