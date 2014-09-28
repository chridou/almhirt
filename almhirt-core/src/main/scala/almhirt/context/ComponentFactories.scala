package almhirt.context

import almhirt.common.AlmFuture
import almhirt.akkax.ComponentFactory

final case class ComponentFactories(
  /** These are the event logs. They will be placed under "/user/{almhirt}/components/event-logs". */ 
  buildEventLogs: AlmhirtContext ⇒ AlmFuture[Seq[ComponentFactory]],
  /** These are the views. They will be placed under "/user/{almhirt}/components/views". */ 
  buildViews: AlmhirtContext ⇒ AlmFuture[Seq[ComponentFactory]],
  /** These are other components. They will be children of  "/user/{almhirt}/components/misc". */ 
  buildMisc: AlmhirtContext ⇒ AlmFuture[Seq[ComponentFactory]],
  /** These are your hosted apps. They will be children of  "/user/{almhirt}/components/apps". */ 
  buildApps: AlmhirtContext ⇒ AlmFuture[Seq[ComponentFactory]],
  /** Build the nexus under  "/user/{almhirt}/components/{nexus}". */ 
  buildNexus: Option[AlmhirtContext ⇒ AlmFuture[ComponentFactory]]
)


object ComponentFactories {
  
  val empty = ComponentFactories(
    _ ⇒ AlmFuture.successful(Seq.empty), 
    _ ⇒ AlmFuture.successful(Seq.empty), 
    _ ⇒ AlmFuture.successful(Seq.empty),
    _ ⇒ AlmFuture.successful(Seq.empty),
    None
  )
}