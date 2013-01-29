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
  def apply(): Almhirt with Disposable =
    Almhirt("Almhirt")
  
  def apply(name: String): Almhirt with Disposable =
    Almhirt(name, None, c => scalaz.Failure(UnspecifiedProblem("Get service not supplied")))
  
  def apply(name: String, getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef]): Almhirt with Disposable =
    Almhirt(name, None, getService)

  def apply(name: String, namesSuffix: Option[String], getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef]): Almhirt with Disposable =
    Almhirt(name, namesSuffix, getService, None)

  def apply(name: String, namesSuffix: Option[String], getService: (Class[_ <: AnyRef]) => AlmValidation[AnyRef], actorSystem: Option[ActorSystem]): Almhirt with Disposable = {
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
      override def dispose() = disposer()
    }
  }

  implicit class AlmhirtOps(theAlmhirt: Almhirt) {
  }
}