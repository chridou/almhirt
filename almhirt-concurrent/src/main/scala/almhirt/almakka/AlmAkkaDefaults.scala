package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import akka.util.Timeout

//trait AlmAkkaDefaults { self: AlmAkkaComponent =>
//  implicit def defaultActorSystem = almAkkaContext.actorSystem
//  implicit def defaultFutureDispatch = almAkkaContext.futureDispatcher
//  implicit def defaultDuration = almAkkaContext.mediumDuration
//  implicit def defaultTimeout = Timeout(defaultDuration)
//}