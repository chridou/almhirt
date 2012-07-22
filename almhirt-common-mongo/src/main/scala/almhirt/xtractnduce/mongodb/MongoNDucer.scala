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

  private def addToBuilder(script: NDuceScript, builder: Builder[(String, Any), DBObject]): Unit =
    script match {
      case SetString(key, value) =>
        builder += key -> value
      case SetInt(key, value) =>
        builder += key -> value
      case SetLong(key, value) =>
        builder += key -> value
      case SetDouble(key, value) =>
        builder += key -> value
      case SetFloat(key, value) =>
        builder += key -> value
      case SetBoolean(key, value) =>
        builder += key -> value
      case SetDecimal(key, value) =>
        builder += key -> value
      case SetDateTime(key, value) =>
        builder += key -> value
      case SetBytes(key, value) =>
        builder += key -> value
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