package almhirt.akkax

import akka.actor.{ Props, ActorRef }

final case class ComponentFactory(props: Props, postAction: ActorRef => Unit)
