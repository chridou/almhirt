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
      case NDuceString(key, value) =>
        Elem(null, key, null, TopScope, Text(value))
      case NDuceStringOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(Text(_)).toSeq: _*)
      case NDuceInt(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceIntOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceLong(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceLongOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceDouble(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceDoubleOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceFloat(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceFloatOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceBoolean(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceBooleanOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceDecimal(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceDecimalOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceDateTime(key, value) =>
        Elem(null, key, null, TopScope, Text(""))
      case NDuceDateTimeOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceUUID(key, value) =>
        Elem(null, key, null, TopScope, Text(value.toString))
      case NDuceUUIDOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map(x => Text(x.toString)).toSeq: _*)
      case NDuceBytes(key, value) =>
        Elem(null, key, null, TopScope, Text(org.apache.commons.codec.binary.Base64.encodeBase64String(value)))
      case NDuceBytesOpt(key, value) =>
        Elem(null, key, null, TopScope, value.map{x => Text(org.apache.commons.codec.binary.Base64.encodeBase64String(x))}.toSeq: _*)
      case NDuceElement(key, scriptElement) =>
        Elem(null, key, null, TopScope, apply(scriptElement))
      case NDuceElementOpt(key, scriptElement) =>
        Elem(null, key, null, TopScope, scriptElement.map{x => apply(x)}.toSeq: _*)
      case NDuceElements(key, scriptElements) =>
        val children = scriptElements map {apply(_)}
        Elem(null, key, null, TopScope, children: _*)
      case NDucePrimitives(key, primitives) =>
        val children = primitives map {v => <value>{v.toString}</value>}
        Elem(null, key, null, TopScope, children: _*)
      case NDuceAggregate(key, values, _) =>
        val children = values map {toXmlElement(_)}
        Elem(null, key, null, TopScope, children: _*)
  }
}