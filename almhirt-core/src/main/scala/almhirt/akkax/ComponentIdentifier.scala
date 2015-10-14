package almhirt.akkax

final case class AppName(value: String) extends AnyVal with Ordered[AppName] {
  def compare(that: AppName): Int = this.value compare that.value
}

final case class ComponentName(value: String) extends AnyVal with Ordered[ComponentName] {
  def compare(that: ComponentName): Int = this.value compare that.value
}

final case class ComponentId(app: AppName, component: ComponentName) extends Ordered[ComponentId] {
  def compare(that: ComponentId): Int =
    if (this.app == that.app) {
      this.component compare that.component
    } else {
      this.app compare that.app
    }
}

trait ActorComponentIdProvider {
  def componentId: ComponentId
}

final case class NodeName(val value: String) extends AnyVal with Ordered[NodeName] {
  def compare(that: NodeName): Int = this.value compare that.value

}

final case class GlobalComponentId(node: NodeName, app: AppName, component: ComponentName) extends Ordered[GlobalComponentId] {
  def compare(that: GlobalComponentId): Int =
    if (this.node == that.node) {
      if (this.app == that.app) {
        this.component compare that.component
      } else {
        this.app compare that.app
      }
    } else {
      this.node compare that.node
    }

  def componentId = ComponentId(app, component)

  def toPathString = s"${node.value}/${app.value}/${component.value}"
}

object GlobalComponentId {
  def apply(node: NodeName, componentId: ComponentId): GlobalComponentId =
    GlobalComponentId(node, componentId.app, componentId.component)

  def apply(componentId: ComponentId)(implicit ctx: almhirt.context.AlmhirtContext): GlobalComponentId =
    GlobalComponentId(ctx.localNodeName, componentId.app, componentId.component)

}
