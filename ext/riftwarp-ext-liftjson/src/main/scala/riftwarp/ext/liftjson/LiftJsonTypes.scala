package riftwarp.ext.liftjson

import riftwarp._
import net.liftweb.json._

case class DimensionLiftJsonAst(manifestation: JValue) extends RiftDimension

class ToolGroupLiftJson extends ToolGroup { val name = "tool_liftjson" }
object ToolGroupLiftJson {
  private val theInstance = new ToolGroupLiftJson()
  def apply() = theInstance
}

import scalaz.syntax.validation._
import almhirt.common._

object DimensionLiftJsonAstToString extends DimensionConverter[DimensionLiftJsonAst, DimensionString] {
  import scalaz.Cord
  val tSource = classOf[DimensionLiftJsonAst]
  val tTarget = classOf[DimensionString]
  def convert(source: DimensionLiftJsonAst): AlmValidation[DimensionString] =
    DimensionString(compact(render(source.manifestation))).success
}

object DimensionLiftJsonAstToNiceString extends DimensionConverter[DimensionLiftJsonAst, DimensionNiceString] {
  import scalaz.Cord
  val tSource = classOf[DimensionLiftJsonAst]
  val tTarget = classOf[DimensionNiceString]
  def convert(source: DimensionLiftJsonAst): AlmValidation[DimensionNiceString] =
    DimensionNiceString(pretty(render(source.manifestation))).success
}

object DimensionLiftJsonAstToCord extends DimensionConverter[DimensionLiftJsonAst, DimensionCord] {
  import scalaz.Cord
  val tSource = classOf[DimensionLiftJsonAst]
  val tTarget = classOf[DimensionCord]
  def convert(source: DimensionLiftJsonAst): AlmValidation[DimensionCord] =
    DimensionCord(compact(render(source.manifestation))).success
}

object DimensionLiftJsonAstToNiceCord extends DimensionConverter[DimensionLiftJsonAst, DimensionNiceCord] {
  import scalaz.Cord
  val tSource = classOf[DimensionLiftJsonAst]
  val tTarget = classOf[DimensionNiceCord]
  def convert(source: DimensionLiftJsonAst): AlmValidation[DimensionNiceCord] =
    DimensionNiceCord(pretty(render(source.manifestation))).success
}

object DimensionStringToLiftJsonAst extends DimensionConverter[DimensionString, DimensionLiftJsonAst] {
  import scalaz.Cord
  val tSource = classOf[DimensionString]
  val tTarget = classOf[DimensionLiftJsonAst]
  def convert(source: DimensionString): AlmValidation[DimensionLiftJsonAst] =
    try {
      DimensionLiftJsonAst(parse(source.manifestation)).success
    } catch {
      case exn => ParsingProblem("Could not parse JSON(LiftJSON-Parser)", input = Some(source.manifestation), cause = Some(CauseIsThrowable(exn))).failure
    }
}

object DimensionCordToLiftJsonAst extends DimensionConverter[DimensionCord, DimensionLiftJsonAst] {
  import scalaz.Cord
  val tSource = classOf[DimensionCord]
  val tTarget = classOf[DimensionLiftJsonAst]
  def convert(source: DimensionCord): AlmValidation[DimensionLiftJsonAst] =
    try {
      DimensionLiftJsonAst(parse(source.manifestation.toString)).success
    } catch {
      case exn => ParsingProblem("Could not parse JSON(LiftJSON-Parser)", input = Some(source.manifestation.toString), cause = Some(CauseIsThrowable(exn))).failure
    }
}
