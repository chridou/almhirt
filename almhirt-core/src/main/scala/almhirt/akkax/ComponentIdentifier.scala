package almhirt.akkax

final case class AppName(value: String) extends AnyVal with Ordered[AppName] {
  def compare(that: AppName): Int = this.value compare that.value 
}


final case class ComponentName(value: String) extends AnyVal with Ordered[ComponentName] {
  def compare(that: ComponentName): Int = this.value compare that.value 
}

final case class ComponentId(app: AppName, component: ComponentName) extends Ordered[ComponentId] {
  def compare(that: ComponentId): Int = 
    if(this.app == that.app) {
      this.component compare that.component
    } else {
      this.app compare that.app
    }
}

trait ActorComponentIdProvider {
  def componentId: ComponentId
}
