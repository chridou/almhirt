package almhirt.xtractnduce.xml

import scala.xml.{Elem, Text}
import almhirt.xtractnduce._

object XmlNDucer {
  def induceFromScript(elem: NDuceElem): Elem = {
    val children = elem.values map {toXmlElement(_)}
    Elem(null, elem.key, null, null, children: _*)
  }

  private def toXmlElement(script: NDuceScript): Elem =
    script match {
      case SetString(key, value) =>
        Elem(null, key, null, null, Text(value))
      case SetInt(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetLong(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetDouble(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetFloat(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetBoolean(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetDecimal(key, value) =>
        Elem(null, key, null, null, Text(value.toString))
      case SetDateTime(key, value) =>
        Elem(null, key, null, null, Text(""))
      case SetBytes(key, value) =>
        Elem(null, key, null, null, Text(org.apache.commons.codec.binary.Base64.encodeBase64String(value)))
      case SetElements(key, elements) =>
        val children = elements map {toXmlElement(_)}
        Elem(null, key, null, null, children: _*)
      case SetPrimitives(key, primitives) =>
        val children = primitives map {v => <value>{v.toString}</value>}
        Elem(null, key, null, null, children: _*)
      case NDuceElem(key, values) =>
        val children = values map {toXmlElement(_)}
        Elem(null, key, null, null, children: _*)
  }
}