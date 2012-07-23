package almhirt.xtractnduce.mongodb

import scala.xml.Elem
import almhirt.xtractnduce._
import com.mongodb.casbah.Imports._
import scala.collection.mutable.Builder

object MongoNDucer {
  import scala.collection.mutable.Builder
  
  def apply(script: NDuceScript)(implicit mapKey: MongoKeyMapper): MongoDBObject = {
    mongoObjectFromScript(script, mapKey)
  }
  
  def apply(script: NDuceScript, idKey: String): MongoDBObject = {
    mongoObjectFromScript(script, MongoKeyMapper.createKeyMapper(idKey))
  }
  
  private def mongoObjectFromScript(script: NDuceScript, mapKey: String => String): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    script.ops foreach {op => addToBuilder(op, builder, mapKey)}
    script.typeInfo foreach {ti => builder += "typeInfo" -> ti}
    builder.result
  }
  
  private def addToBuilder(scriptOp: NDuceScriptOp, builder: Builder[(String, Any), DBObject], mapKey: String => String): Unit =
    scriptOp match {
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
        builder += key -> apply(value)
      case SetElementOpt(key, value) =>
        value foreach {v => builder += key -> mongoObjectFromScript(v, identity)}
      case SetElements(key, elements) =>
        val children = elements map {mongoObjectFromScript(_, identity)}
        builder += key -> MongoDBList(children: _*)
      case SetPrimitives(key, primitives) =>
        builder += key -> MongoDBList(primitives: _*)
      case element @ NDuceElem(name, _, _) =>
        builder += name -> mongoObjectFromScript(element, identity)
  }
}