package riftwarp.std

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object ToJsonStringDematerializer extends Dematerializer[String @@ WarpTags.Json] {
  override val channels = Set(WarpChannels.`rift-json`)
  def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): String @@ WarpTags.Json =
    WarpTags.JsonString(ToJsonCordDematerializer.dematerialize(what).toString)
}

object ToNoisyXmlStringDematerializer extends Dematerializer[String @@ WarpTags.Xml] {
  override val channels = Set(WarpChannels.`rift-xml`)
  def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): String @@ WarpTags.Xml =
    WarpTags.XmlString(ToNoisyXmlElemDematerializer.dematerialize(what).toString)
}

object ToHtmlStringDematerializer extends Dematerializer[String @@ WarpTags.Html] {
  override val channels = Set(WarpChannels.`rift-html`)
  def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): String @@ WarpTags.Html =
    WarpTags.HtmlString(ToHtmlElemDematerializer.dematerialize(what).toString)
}

object FromJsonStringRematerializer extends Rematerializer[String @@ WarpTags.Json] {
  import scala.util.parsing.json._
  override val channels = Set(WarpChannels.`rift-json`)
  def rematerialize(what: String @@ WarpTags.Json, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] = {
    val wwhat = Tag.unwrap(what)
    if (wwhat.startsWith("{") || wwhat.startsWith("[")) {
      val parser = new scala.util.parsing.json.Parser
      parser.phrase(parser.root)(new parser.lexical.Scanner(wwhat: String)) match {
        case parser.Success(result, _) ⇒
          FromStdLibJsonRematerializer.rematerialize(WarpTags.JsonStdLib(result))
        case parser.NoSuccess(msg, _) ⇒
          ParsingProblem(msg, Some(wwhat)).failure
      }
    } else if (wwhat.startsWith("\"") && wwhat.endsWith("\"")) {
      WarpString(wwhat.substring(1, wwhat.length() - 1)).success
    } else if (wwhat == "true") {
      WarpBoolean(true).success
    } else if (wwhat == "false") {
      WarpBoolean(false).success
    } else if (wwhat.toDoubleAlm.isSuccess) {
      WarpDouble(wwhat.toDoubleAlm.forceResult).success
    } else {
      ParsingProblem("Input is no JSON nor a primitive type", Some(wwhat)).failure
    }
  }
}

object FromJsonCordRematerializer extends Rematerializer[Cord @@ WarpTags.Json] {
  import scala.util.parsing.json._
  override val channels = Set(WarpChannels.`rift-json-cord`)
  def rematerialize(what: Cord @@ WarpTags.Json, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    FromJsonStringRematerializer.rematerialize(WarpTags.JsonString(what.toString), options)
}

object FromXmlStringRematerializer extends Rematerializer[String @@ WarpTags.Xml] {
  override val channels = Set(WarpChannels.`rift-xml`)
  def rematerialize(what: String @@ WarpTags.Xml, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    inTryCatch { scala.xml.XML.loadString(Tag.unwrap(what)) }.flatMap(xml ⇒
      FromStdLibXmlRematerializer.rematerialize(xml))
}