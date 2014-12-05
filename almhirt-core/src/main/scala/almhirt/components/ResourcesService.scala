package almhirt.components

import akka.actor._
import almhirt.common._
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

  def receive: Receive = {
    case "ShowLocales" ⇒
      inTryCatch { fixedResources.localesTree.flatten.map(_.toLanguageTag()) }.fold(
        fail ⇒ {
          logWarning(s"There are no supported locales:\n$fail")
        },
        succ ⇒ logInfo(s"""Supported locales(first is root):\n${succ.mkString(", ")}"""))
    case ResourcesService.GetResources ⇒ sender ! ResourcesService.Resources(fixedResources)
  }

  override def preStart() {
    context.parent ! ResourcesService.ResourcesInitialized
    self ! "ShowLocales"
  }

}

