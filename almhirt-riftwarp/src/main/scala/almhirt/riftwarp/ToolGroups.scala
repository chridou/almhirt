package almhirt.riftwarp

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
