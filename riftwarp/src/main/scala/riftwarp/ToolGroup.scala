package riftwarp

trait ToolGroup extends Equals {
  def name: String

  def canEqual(other: Any) = {
    other.isInstanceOf[riftwarp.ToolGroup]
  }

  override def equals(other: Any) = {
    other match {
      case that: riftwarp.ToolGroup => that.canEqual(ToolGroup.this) && name == that.name
      case _ => false
    }
  }

  override def hashCode() = {
    name.hashCode
  }
  
  override def toString() = name
}


class ToolGroupRiftStd extends ToolGroup { val name = "tool_riftstd" }
object ToolGroupRiftStd {
  private val theInstance = new ToolGroupRiftStd()
  def apply() = theInstance
}

class ToolGroupStdLib extends ToolGroup { val name = "tool_stdlib" }
object ToolGroupStdLib {
  private val theInstance = new ToolGroupStdLib()
  def apply() = theInstance
}


object ToolGroup {
  val StdLib = ToolGroupStdLib()
  val RiftStd = ToolGroupRiftStd()
}
