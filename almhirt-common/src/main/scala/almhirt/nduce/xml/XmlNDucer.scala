package almhirt.nduce.xml

import scala.xml.{Elem, Text}
import org.joda.time.DateTime
import almhirt.nduce._

object XmlNDucer {
  def inducefromScript(elem: NDuceElem): Elem = {
    val children = elem.values map {toXmlElement(_)}
    Elem("", elem.key, null, null, children: _*)
  }

  private def toXmlElement(script: NDuceScript): Elem =
    script match {
      case SetString(key, value) =>
        Elem("", key, null, null, Text(value))
      case SetInt(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetLong(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetDouble(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetFloat(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetBoolean(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetDecimal(key, value) =>
        Elem("", key, null, null, Text(value.toString))
      case SetDateTime(key, value) =>
        Elem("", key, null, null, Text(""))
      case SetBytes(key, value) =>
        Elem("", key, null, null, Text(org.apache.commons.codec.binary.Base64.encodeBase64String(value)))
      case SetElements(key, elements) =>
        val children = elements map {toXmlElement(_)}
        Elem("", key, null, null, children: _*)
      case SetPrimitives(key, primitives) =>
        val children = primitives map {v => <value>{v.toString}</value>}
        Elem("", key, null, null, children: _*)
      case NDuceElem(key, values) =>
        val children = values map {toXmlElement(_)}
        Elem("", key, null, null, children: _*)
  }
}