package almhirt.domain

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.almfuture.all._
import scala.reflect.ClassTag

trait AggregateRootRepository { actor: akka.actor.Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def receiveRepositoryMsg: Receive
}

object AggregateRootRepository {
  import almhirt.configuration._
  import com.typesafe.config._
  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configSection: Config, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
    for {
      cellAskMaxDuration <- configSection.v[FiniteDuration]("cell-ask-max-duration")
      cacheAskMaxDuration <- configSection.v[FiniteDuration]("cell-cache-ask-max-duration")
    } yield Props(
      new almhirt.domain.impl.AggregateRootRepositoryImpl[TAR, TEvent](theAlmhirt, cellCache, cellAskMaxDuration, cacheAskMaxDuration))

  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configPath: String, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props[TAR, TEvent](cellCache, configSection, theAlmhirt))

  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
    props[TAR, TEvent](cellCache, "almhirt.repositories", theAlmhirt)

  def apply[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configSection: Config, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[ActorRef] =
    for {
      theProps <- props[TAR, TEvent](cellCache, configSection, theAlmhirt)
      useRouting <- configSection.v[Boolean]("use-routing")
      routingProperties <- if (useRouting) configSection.v[java.util.Properties]("routing-config").map(Some(_)) else None.success
      numActors <- routingProperties match {
        case None => 1.success
        case Some(props) =>
          if (props.containsKey(tagAr.runtimeClass.getName()))
            props.getProperty(tagAr.runtimeClass.getName()).toIntAlm
          else
            1.success
      }
    } yield {
      if (numActors > 1)
        theAlmhirt.actorSystem.actorOf(Props(new AggregateRootRepositoryRouter(numActors, theProps)), s"aggregate-root-repository-${tagAr.runtimeClass.getSimpleName().toLowerCase()}")
      else
        theAlmhirt.actorSystem.actorOf(theProps, s"aggregate-root-repository-${tagAr.runtimeClass.getSimpleName().toLowerCase()}")
    }

  def apply[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configPath: String, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[ActorRef] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      theRepo <- apply[TAR, TEvent](cellCache, configSection, theAlmhirt)
    } yield theRepo

  def apply[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[ActorRef] =
    apply[TAR, TEvent](cellCache, "almhirt.repositories", theAlmhirt)

}