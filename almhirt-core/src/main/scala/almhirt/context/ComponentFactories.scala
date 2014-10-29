package almhirt.context

import almhirt.common._
import almhirt.problem.Severity
import almhirt.akkax.ComponentFactory

final case class ComponentFactoryBuilderEntry(buildFactory: AlmhirtContext ⇒ AlmFuture[ComponentFactory], failureSeverity: Severity) {
  def toSeq = Seq(this)
}

object ComponentFactoryBuilderEntry {
  def apply(buildFactory: AlmhirtContext ⇒ AlmFuture[ComponentFactory]): ComponentFactoryBuilderEntry = 
    ComponentFactoryBuilderEntry(buildFactory, CriticalSeverity)
}

final case class ComponentFactories(
  /** These are the event logs. They will be placed under "/user/{almhirt}/components/event-logs". */
  buildEventLogs: Seq[ComponentFactoryBuilderEntry],
  /** These are the views. They will be placed under "/user/{almhirt}/components/views". */
  buildViews: Seq[ComponentFactoryBuilderEntry],
  /** These are other components. They will be children of  "/user/{almhirt}/components/misc". */
  buildMisc: Seq[ComponentFactoryBuilderEntry],
  /** These are your hosted apps. They will be children of  "/user/{almhirt}/components/apps". */
  buildApps: Seq[ComponentFactoryBuilderEntry],
  /** Build the nexus under  "/user/{almhirt}/components/{nexus}". */
  buildNexus: Option[AlmhirtContext ⇒ AlmFuture[ComponentFactory]])

object ComponentFactories {
  val empty = ComponentFactories(
    Seq.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty,
    None)
}