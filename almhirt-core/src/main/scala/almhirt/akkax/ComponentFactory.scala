package almhirt.akkax

import scalaz.syntax.validation._
import akka.actor.{ Props, ActorRef, ActorRefFactory, ActorContext }
import almhirt.common._

/**
 * creator is the Actor that created createdActor and localActorRefFactory is the creator's ActorContext
 *  An ActorContext may not be considered thread safe!
 */
final case class PostActionParams(createdActor: ActorRef, creator: ActorRef, localActorRefFactory: ActorRefFactory)

final case class PostAction(f: PostActionParams => AlmValidation[Unit]) extends Function3[ActorRef, ActorRef, ActorRefFactory, AlmValidation[Unit]] {
  final def apply(createdActor: ActorRef, creator: ActorRef, localActorRefFactory: ActorRefFactory): AlmValidation[Unit] =
    executePostAction(PostActionParams(createdActor, creator, localActorRefFactory))

  def executePostAction(postActionParams: PostActionParams): AlmValidation[Unit] =
    f(postActionParams)
}

object PostAction {
  val noOp: PostAction = PostAction(_ => ().success)
}

final case class ComponentFactory(props: Props, name: Option[String], postAction: PostAction) extends Function1[ActorContext, AlmValidation[ActorRef]] {
  final def apply(creatorContext: ActorContext): AlmValidation[ActorRef] = create(creatorContext)

  def create(creatorContext: ActorContext): AlmValidation[ActorRef] = {
    import almhirt.almvalidation.kit._
    for {
      actorRef <- inTryCatch {
        name match {
          case Some(n) =>
            creatorContext.actorOf(props, n)
          case None =>
            creatorContext.actorOf(props)
        }
      }
      _ <- postAction(actorRef, creatorContext.self, creatorContext).fold(
        problem => {
          creatorContext.stop(actorRef)
          UnspecifiedProblem(s"The post action failed. Killing created actor ${actorRef}. Cause:\n$problem").failure
        },
        _ => actorRef.success)
    } yield {
      actorRef
    }
  }
}

object ComponentFactory {
  def apply(props: Props): ComponentFactory = ComponentFactory(props, None, PostAction.noOp)
  def apply(props: Props, name: String): ComponentFactory = ComponentFactory(props, Some(name), PostAction.noOp)

  implicit class ComponentFactoryOps(self: ComponentFactory) {
    def withName(name: String): ComponentFactory =
      self.copy(name = Some(name))

    def withPostAction1(postAction: PostAction): ComponentFactory =
      self.copy(postAction = postAction)

    def withPostAction2(f: PostActionParams => AlmValidation[Unit]): ComponentFactory =
      self.copy(postAction = PostAction(f))

    def withPostAction3(f: (ActorRef, ActorRef, ActorRefFactory) => AlmValidation[Unit]): ComponentFactory =
      self.copy(postAction = PostAction(params => f(params.createdActor, params.creator, params.localActorRefFactory)))
  }
}
