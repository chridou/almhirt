package riftwarp.ext.liftjson

import riftwarp._
import net.liftweb.json._

case class DimensionLiftJsonAst(manifestation: JValue) extends RiftDimension


class ToolGroupLiftJson extends ToolGroup { val name = "tool_liftjson" }
object ToolGroupLiftJson {
  private val theInstance = new ToolGroupLiftJson()
  def apply() = theInstance
}

object LiftJson {
  val toolGroup = ToolGroupLiftJson()
}