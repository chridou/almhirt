package almhirt.xtractnduce.xml

import scala.xml.{Elem, Text, TopScope}
import almhirt.xtractnduce._

object XmlNDucer {
  def apply(script: NDuceScript): Elem = {
    val children = script.ops map {toXmlElement(_)}
    Elem(null, script.name, null, TopScope, children: _*)
  }
  
  private def toXmlElement(script: NDuceScriptOp): Elem =
    script match {
      case SetString(key, value) =>
        Elem(null, key, null, TopScope, Text(value))
      case SetStringOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(Text(_)).toSeq: _*)
      case SetInt(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetIntOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetLong(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetLongOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetDouble(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetDoubleOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetFloat(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetFloatOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetBoolean(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetBooleanOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetDecimal(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case SetDecimalOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetDateTime(key, value) =>
        Elem(null, key, null, TopScope, Text(""))
      case SetDateTimeOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case SetBytes(key, value) =>
        Elem(null, key, null, TopScope, Text(org.apache.commons.codec.binary.Base64.encodeBase64String(value)))
      case SetBytesOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map{x => Text(org.apache.commons.codec.binary.Base64.encodeBase64String(x))}.toSeq: _*)
      case SetElement(key, scriptElement) =>
        Elem(null, key, null, TopScope, apply(scriptElement))
      case SetElementOpt(key, scriptElement) =>
        Elem(null, key, null, TopScope, scriptElement.map{x => apply(x)}.toSeq: _*)
      case SetElements(key, scriptElements) =>
        val children = scriptElements map {apply(_)}
        Elem(null, key, null, TopScope, children: _*)
      case SetPrimitives(key, primitives) =>
        val children = primitives map {v => <value>{v.toString}</value>}
        Elem(null, key, null, TopScope, children: _*)
      case NDuceElem(key, values, _) =>
        val children = values map {toXmlElement(_)}
        Elem(null, key, null, TopScope, children: _*)
  }
}