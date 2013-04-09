package riftwarp.impl.rematerializers

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import riftwarp.components.HasRecomposers
import riftwarp.components.ExtractorFactory

class FromStdLibJsonExtractor(values: Map[String, Any], path: List[String])(implicit hasRecomposers: HasRecomposers) extends ExtractorTemplate[DimensionStdLibJson](path) with NoneHandlingExtractor {
  override val rematerializer = FromStdLibJsonRematerializer

  override def getValue(ident: String): AlmValidation[Any] =
    values.get(ident) match {
      case Some(v) => v.success
      case None => KeyNotFoundProblem(s"No value found for key '$ident'").failure
    }

  override def spawnNew(ident: String)(value: Any): AlmValidation[Extractor] = FromStdLibJsonExtractor.create(value, ident :: path)

  override def hasValue(ident: String) = {
    values.get(ident) match {
      case Some(v) =>
        v != null
      case None =>
        false
    }
  }

  override def getRiftDescriptor: AlmValidation[RiftDescriptor] =
    getWith(RiftDescriptor.defaultKey, riftwarp.serialization.common.RiftDescriptorRecomposer.recompose)

  override def tryGetRiftDescriptor: AlmValidation[Option[RiftDescriptor]] =
    if (hasValue(RiftDescriptor.defaultKey)) getRiftDescriptor.map(Some(_)) else None.success
}

object FromStdLibJsonExtractor extends ExtractorFactory[DimensionStdLibJson] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionStdLibJson].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(stdLibJsonMap: Map[String, Any], path: List[String])(implicit hasRecomposers: HasRecomposers): FromStdLibJsonExtractor =
    new FromStdLibJsonExtractor(stdLibJsonMap, path)
  def apply(stdLibJsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers): FromStdLibJsonExtractor =
    new FromStdLibJsonExtractor(stdLibJsonMap, Nil)
  def create(from: Any, path: List[String])(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    from match {
      case m: Map[_, _] => (new FromStdLibJsonExtractor(m.asInstanceOf[Map[String, Any]], path)).success
      case x => InvalidCastProblem(s"""Cannot create a FromStdLibJsonExtractor because '${x.getClass.getName()}' is not a Map[String, Any]. The path is "${path.mkString("/")}"""").failure
    }
  override def createExtractor(from: DimensionStdLibJson)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    create(from.manifestation, Nil)
}

object FromStdLibJsonStringExtractor extends ExtractorFactory[DimensionString] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  import scala.util.parsing.json._

  def apply(json: String)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] = {
    def transformMap(m: Map[String, Any]) =
      m.transform {
        case (k, v) => resolveType(v)
      }

    def resolveType(input: Any): Any = input match {
      case JSONObject(data) => transformMap(data)
      case JSONArray(data) => data.map(resolveType)
      case x => x
    }
    // Always instantiate a new parser because the singleton from the standard library is not threadsafe!
    val parser = new scala.util.parsing.json.Parser
    parser.phrase(parser.root)(new parser.lexical.Scanner(json)) match {
      case parser.Success(result, _) =>
        result match {
          case JSONObject(data) =>
            FromStdLibJsonExtractor(transformMap(data)).success
          case x =>
            UnspecifiedProblem("'%s' is not valid for dematerializing. A Map[String, Any] is required".format(x)).failure
        }

      case parser.NoSuccess(msg, _) =>
        ParsingProblem(msg).withInput(json).failure
    }
  }

  override def createExtractor(from: DimensionString)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    apply(from.manifestation)(hasRecomposers)
}

object FromStdLibJsonCordExtractor extends ExtractorFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  import scalaz.Cord

  def apply(json: Cord)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    FromStdLibJsonStringExtractor.createExtractor(DimensionString(json.toString))(hasRecomposers)
  def apply(json: DimensionCord)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    FromStdLibJsonStringExtractor.createExtractor(DimensionString(json.manifestation.toString))(hasRecomposers)
  override def createExtractor(from: DimensionCord)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    apply(from)(hasRecomposers)
}

