package almhirt.context

import almhirt.common.AlmFuture
import almhirt.akkax.ComponentFactory

final case class ComponentFactories(
  /** These are the event logs. They will be placed under "/user/{almhirt}/components/event-logs". */ 
  buildEventLogs: AlmhirtContext => AlmFuture[Map[String, ComponentFactory]],
  /** These are the views. They will be placed under "/user/{almhirt}/components/views". */ 
  buildViews: AlmhirtContext => AlmFuture[Map[String, ComponentFactory]],
  /** These are other components. They will be children of  "/user/{almhirt}/components/misc". */ 
  buildMisc: AlmhirtContext => AlmFuture[Map[String, ComponentFactory]],
  /** These are your hosted apps. They will be children of  "/user/{almhirt}/components/apps". */ 
  buildApps: AlmhirtContext => AlmFuture[Map[String, ComponentFactory]]
)


object ComponentFactories {
  
  val empty = ComponentFactories(
    _ => AlmFuture.successful(Map.empty), 
    _ => AlmFuture.successful(Map.empty), 
    _ => AlmFuture.successful(Map.empty),
    _ => AlmFuture.successful(Map.empty)
  )
}