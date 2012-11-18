package almhirt.riftwarp

trait ToolGroup extends Equals {
  def name: String

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.ToolGroup]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.ToolGroup => that.canEqual(ToolGroup.this) && name == that.name
      case _ => false
    }
  }

  override def hashCode() = {
    name.hashCode
  }
  
  override def toString() = name
}

object ToolGroup {
  def apply(aName: String): ToolGroup = new ToolGroup { val name = aName }
}
