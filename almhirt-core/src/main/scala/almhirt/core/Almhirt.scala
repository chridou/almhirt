package almhirt.core

import scalaz.std._
import akka.actor.ActorSystem
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import almhirt.messaging._
import com.typesafe.config.Config
import akka.event.Logging
import almhirt.environment.configuration.CanCreateSuffixedName
import akka.actor.PoisonPill

trait Almhirt extends HasActorSystem
  with HasMessageHub
  with CanCreateUuidsAndDateTimes
  with HasDurations
  with HasExecutionContext
  with HasServices
  with CanPublishMessages
  with CanPublishItems {
  def log: akka.event.LoggingAdapter
}

object Almhirt {
  def quickCreateWithSystem(): Almhirt with Disposable with CanCreateSuffixedName =
    quickCreateWithSystem("Almhirt")

  def quickCreateWithSystem(name: String): Almhirt with Disposable with CanCreateSuffixedName =
    quickCreateWithSystem(name, None, c => scalaz.Failure(UnspecifiedProblem("Get service not supplied")))

  def quickCreateWithSystem(name: String, getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef]): Almhirt with Disposable with CanCreateSuffixedName =
    quickCreateWithSystem(name, None, getService)

    
  def quickCreateWithSystem(name: String, namesSuffix: Option[String], getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef]): Almhirt with Disposable with CanCreateSuffixedName =
    createInternal(name, namesSuffix, getService, None)

  def quickCreateFromSystem(namesSuffix: Option[String], getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef], actorSystem: ActorSystem): Almhirt with Disposable with CanCreateSuffixedName =
    createInternal("", namesSuffix, getService, Some(actorSystem))

  def quickCreateFromSystem(namesSuffix: Option[String], actorSystem: ActorSystem): Almhirt with Disposable with CanCreateSuffixedName =
    createInternal("", namesSuffix, c => scalaz.Failure(UnspecifiedProblem("Get service not supplied")), Some(actorSystem))

  def quickCreateFromSystem(actorSystem: ActorSystem): Almhirt with Disposable with CanCreateSuffixedName =
    createInternal("", None, c => scalaz.Failure(UnspecifiedProblem("Get service not supplied")), Some(actorSystem))
    
  private def createInternal(name: String, namesSuffix: Option[String], getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef], actorSystem: Option[ActorSystem]): Almhirt with Disposable with CanCreateSuffixedName = {
    def getName(name: String) = option.cata(namesSuffix)(aSuffix => s"${name}_$aSuffix", name)

    val (theActorSystem, disposer) = option.cata(actorSystem)(
      some => (some, () => ()),
      { val actorSystem = ActorSystem(name); (actorSystem, () => { actorSystem.shutdown(); actorSystem.awaitTermination() }) })

    val theMessageHub = MessageHub(getName("MessageHub"))(theActorSystem, theActorSystem.dispatchers.defaultGlobalDispatcher)

    new Almhirt with Disposable with HasDefaultDurations with PublishesOnMessageHub with CanCreateSuffixedName {
      override val actorSystem = theActorSystem
      override val messageHub = theMessageHub
      override val executionContext = theActorSystem.dispatchers.defaultGlobalDispatcher
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = getService(clazz)
      override val log = Logging(actorSystem, classOf[Almhirt])
      override def createSuffixedName(aName: String) = getName(name)
      override def dispose() = { theMessageHub.close(); disposer() }
    }
  }

  implicit class AlmhirtOps(theAlmhirt: Almhirt) {
  }
}