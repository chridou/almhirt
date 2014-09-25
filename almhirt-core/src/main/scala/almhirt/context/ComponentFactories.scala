package almhirt.context

import akka.actor.Props
import almhirt.common.AlmFuture

final case class ComponentFactories(
  /** These are the event logs. They will be placed under "/user/{almhirt}/components/event-logs". */ 
  buildEventLogs: AlmhirtContext => AlmFuture[Map[String, Props]],
  /** These are the views. They will be placed under "/user/{almhirt}/components/views". */ 
  buildViews: AlmhirtContext => AlmFuture[Map[String, Props]],
  /** These are other components. They will be children of  "/user/{almhirt}/components/misc". */ 
  buildMisc: AlmhirtContext => AlmFuture[Map[String, Props]]
)

object ComponentFactories {
  val empty = ComponentFactories(
    _ => AlmFuture.successful(Map.empty), 
    _ => AlmFuture.successful(Map.empty), 
    _ => AlmFuture.successful(Map.empty)
  )
}