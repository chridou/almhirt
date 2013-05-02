package riftwarp.std

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

object ToJsonStringDematerializer extends Dematerializer[String @@ WarpTags.Json] {
  def dematerialize(what: WarpPackage): String @@ WarpTags.Json =
    WarpTags.JsonString(ToJsonCordDematerializer.dematerialize(what).toString)
}

object ToNoisyXmlStringDematerializer extends Dematerializer[String @@ WarpTags.Xml] {
  def dematerialize(what: WarpPackage): String @@ WarpTags.Xml =
    WarpTags.XmlString(ToNoisyXmlElemDematerializer.dematerialize(what).toString)
}

object FromJsonStringRematerializer extends Rematerializer[String @@ WarpTags.Json] {
  import scala.util.parsing.json._
  def rematerialize(what: String @@ WarpTags.Json): AlmValidation[WarpPackage] = {
    val parser = new scala.util.parsing.json.Parser
    parser.phrase(parser.root)(new parser.lexical.Scanner(what)) match {
      case parser.Success(result, _) =>
        FromStdLibJsonRematerializer.rematerialize(WarpTags.JsonStdLib(result))
      case parser.NoSuccess(msg, _) =>
        ParsingProblem(msg).withInput(what).failure
    }
  }
}

object FromJsonCordRematerializer extends Rematerializer[Cord @@ WarpTags.Json] {
  import scala.util.parsing.json._
  def rematerialize(what: Cord @@ WarpTags.Json): AlmValidation[WarpPackage] = {
    val input = what.toString
    val parser = new scala.util.parsing.json.Parser
    parser.phrase(parser.root)(new parser.lexical.Scanner(input)) match {
      case parser.Success(result, _) =>
        FromStdLibJsonRematerializer.rematerialize(WarpTags.JsonStdLib(result))
      case parser.NoSuccess(msg, _) =>
        ParsingProblem(msg).withInput(input).failure
    }
  }
}

object FromXmlStringRematerializer extends Rematerializer[String @@ WarpTags.Xml] {
  def rematerialize(what: String @@ WarpTags.Xml): AlmValidation[WarpPackage] =
    inTryCatch { scala.xml.XML.loadString(what) }.flatMap(xml =>
      FromStdLibXmlRematerializer.rematerialize(xml))
}