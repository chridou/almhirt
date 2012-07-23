package almhirt.xtractnduce.mongodb

import scala.xml.Elem
import almhirt.xtractnduce._
import com.mongodb.casbah.Imports._
import scala.collection.mutable.Builder

object MongoNDucer {
  import scala.collection.mutable.Builder
  def induceFromScript(elem: NDuceElem): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    addToBuilder(elem, builder)
    builder.result
  }

  private def addToBuilder(script: NDuceScriptOp, builder: Builder[(String, Any), DBObject]): Unit =
    script match {
      case SetString(key, value) =>
        builder += key -> value
      case SetStringOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetInt(key, value) =>
        builder += key -> value
      case SetIntOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetLong(key, value) =>
        builder += key -> value
      case SetLongOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetDouble(key, value) =>
        builder += key -> value
      case SetDoubleOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetFloat(key, value) =>
        builder += key -> value
      case SetFloatOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetBoolean(key, value) =>
        builder += key -> value
      case SetBooleanOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetDecimal(key, value) =>
        builder += key -> value
      case SetDecimalOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetDateTime(key, value) =>
        builder += key -> value
      case SetDateTimeOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetBytes(key, value) =>
        builder += key -> value
      case SetBytesOpt(key, value) =>
        value foreach {v => builder += key -> v}
      case SetElement(key, value) =>
        builder += key -> induceFromScript(value)
      case SetElementOpt(key, value) =>
        value foreach {v => builder += key -> induceFromScript(v)}
      case SetElements(key, elements) =>
        val children = elements map {induceFromScript(_)}
        builder += key -> MongoDBList(children: _*)
      case SetPrimitives(key, primitives) =>
        val children = primitives map {v => <value>{v.toString}</value>}
        Elem("", key, null, null, children: _*)
      case element @ NDuceElem(key, values) =>
        builder += key -> induceFromScript(element)
  }
}