package riftwarpx.sprayjson

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import spray.json._

object FromJsonStringRematerializer extends Rematerializer[String @@ WarpTags.Json] {
  val channel = "json"
  val dimension = classOf[String].getName()
  def rematerialize(what: String @@ WarpTags.Json, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] = {
    if (what.startsWith("{") || what.startsWith("[")) {
      try {
        val jsonAst = what.asJson
        FromSprayJsonRematerializer.rematerialize(jsonAst)
      } catch {
        case scala.util.control.NonFatal(exn) =>
          ParsingProblem(s"""Could not parse JSON: "${exn.getMessage()}"""", args = Map("parser" -> "spray-json")).failure
      }
    } else if (what.startsWith("\"") && what.endsWith("\"")) {
      WarpString(what.substring(1, what.length() - 1)).success
    } else if (what == "true") {
      WarpBoolean(true).success
    } else if (what == "false") {
      WarpBoolean(false).success
    } else if (what.toDoubleAlm.isSuccess) {
      WarpDouble(what.toDoubleAlm.forceResult).success
    } else {
      ParsingProblem("Input is neither JSON nor a primitive type", Some(what)).failure
    }
  }
}