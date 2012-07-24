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
      case NDuceString(key, value) =>
        builder += mapKey(key) -> value
      case NDuceStringOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceInt(key, value) =>
        builder += mapKey(key) -> value
      case NDuceIntOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceLong(key, value) =>
        builder += mapKey(key) -> value
      case NDuceLongOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceDouble(key, value) =>
        builder += mapKey(key) -> value
      case NDuceDoubleOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceFloat(key, value) =>
        builder += mapKey(key) -> value
      case NDuceFloatOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceBoolean(key, value) =>
        builder += mapKey(key) -> value
      case NDuceBooleanOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceDecimal(key, value) =>
        builder += mapKey(key) -> value
      case NDuceDecimalOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceDateTime(key, value) =>
        builder += mapKey(key) -> value
      case NDuceDateTimeOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceBytes(key, value) =>
        builder += mapKey(key) -> value
      case NDuceBytesOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> v}
      case NDuceElement(key, value) =>
        builder += mapKey(key) -> apply(value)
      case NDuceElementOpt(key, value) =>
        value foreach {v => builder += mapKey(key) -> mongoObjectFromScript(v, identity)}
      case NDuceElements(key, elements) =>
        val children = elements map {mongoObjectFromScript(_, identity)}
        builder += key -> MongoDBList(children: _*)
      case NDucePrimitives(key, primitives) =>
        builder += key -> MongoDBList(primitives: _*)
      case agg @ NDuceAggregate(name, _, _) =>
        builder += name -> mongoObjectFromScript(agg, identity)
  }
}