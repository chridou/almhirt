package almhirt.components

import akka.actor._
import almhirt.context.AlmhirtContext
import almhirt.problem.ProblemCause
import almhirt.i18n.AlmResources
import almhirt.akkax._

object ResourcesService {
  sealed trait ResourcesServiceMessage

  case object ResourcesInitialized extends ResourcesServiceMessage
  final case class InitializeResourcesFailed(cause: ProblemCause) extends ResourcesServiceMessage

  case object GetResources extends ResourcesServiceMessage
  final case class Resources(resources: AlmResources) extends ResourcesServiceMessage

  val actorname = "resources-service"

  def propsFromPrebuiltResources(resources: AlmResources)(implicit almhirtContext: AlmhirtContext): Props =
    Props(new FixedResourcesService(resources))

  def emptyProps(implicit almhirtContext: AlmhirtContext): Props =
    propsFromPrebuiltResources(AlmResources.empty)
}

private[almhirt] class FixedResourcesService(fixedResources: AlmResources)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
  logInfo(s"""Supported locales:\n${fixedResources.supportedLocales.mkString(", ")}""")
  
  def receive: Receive = {
    case ResourcesService.GetResources ⇒ sender ! ResourcesService.Resources(fixedResources)
  }

  override def preStart() {
    context.parent ! ResourcesService.ResourcesInitialized
  }
}

