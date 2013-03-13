package riftwarp.impl.rematerializers

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components.HasRecomposers
import riftwarp.components.ExtractorFactory

class FromStdLibJsonExtractor(values: Map[String, Any], fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers) extends ExtractorTemplate[DimensionStdLibJson](fetchBlobData) with NoneHandlingExtractor {
  override val rematerializer = FromStdLibJsonRematerializer

  def getValue(ident: String): AlmValidation[Any] =
    values.get(ident) match {
      case Some(v) => v.success
      case None => KeyNotFoundProblem(s"No value found for key '$ident'").failure
    }

  def spawnNew(value: Any): AlmValidation[Extractor] = FromStdLibJsonExtractor.createExtractor(DimensionStdLibJson(value), fetchBlobData)

  def hasValue(ident: String) = {
    import scala.util.parsing.json._
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

  def apply(stdLibJsonMap: Map[String, Any], fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): FromStdLibJsonExtractor =
    new FromStdLibJsonExtractor(stdLibJsonMap, fetchBlobs)
  def apply(stdLibJsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers): FromStdLibJsonExtractor =
    new FromStdLibJsonExtractor(stdLibJsonMap, NoFetchBlobFetch)
  def createExtractor(from: DimensionStdLibJson, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    from.manifestation match {
      case m: Map[_, _] => apply(m.asInstanceOf[Map[String, Any]]).success
      case x => InvalidCastProblem(s"Cannot create a FromStdLibJsonExtractor beacuse '${x.getClass.getName()}' is not a Map[String, Any]").failure
    }
}

object FromStdLibJsonStringExtractor extends ExtractorFactory[DimensionString] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  import scala.util.parsing.json._

  def apply(json: String, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] = {
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
            FromStdLibJsonExtractor(transformMap(data), fetchBlobs).success
          case x =>
            UnspecifiedProblem("'%s' is not valid for dematerializing. A Map[String, Any] is required".format(x)).failure
        }

      case parser.NoSuccess(msg, _) =>
        ParsingProblem(msg).withInput(json).failure
    }
  }

  def createExtractor(from: DimensionString, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    apply(from.manifestation, fetchBlobs)(hasRecomposers)
}

object FromStdLibJsonCordExtractor extends ExtractorFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  import scalaz.Cord

  def apply(json: Cord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    FromStdLibJsonStringExtractor.createExtractor(DimensionString(json.toString), fetchBlobs)(hasRecomposers)
  def apply(json: DimensionCord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    FromStdLibJsonStringExtractor.createExtractor(DimensionString(json.manifestation.toString), fetchBlobs)(hasRecomposers)
  def createExtractor(from: DimensionCord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibJsonExtractor] =
    apply(from, fetchBlobs)(hasRecomposers)
}

