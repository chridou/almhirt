package almhirt.commanding

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.domain._

trait HasCommandActionHandlers[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent] {
  def addCreatingActionHandler[TAction <: CreatorCommandAction](handler: CreatingActionHandler[TAR, TEvent, TAction])(implicit tag: ClassTag[TAction])
  def addMutatingActionHandler[TAction <: MutatorCommandAction](handler: MutatingActionHandler[TAR, TEvent, TAction])(implicit tag: ClassTag[TAction])

  def getCreatingHandler(action: CreatorCommandAction { type Event = TEvent; type AR = TAR }): AlmValidation[CreatingActionHandler[TAR, TEvent, _]]
  def getMutatingHandler(action: MutatorCommandAction { type Event = TEvent; type AR = TAR }): AlmValidation[MutatingActionHandler[TAR, TEvent, _]]
}

object HasCommandActionHandlers {
  implicit class HasCommandActionHandlersOps[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](hasActionHandlers: HasCommandActionHandlers[TAR, TEvent]) {
    def executeCreatingHandler(action: CreatorCommandAction { type Event = TEvent; type AR = TAR })(implicit theAlmhirt: Almhirt): AlmFuture[UpdateRecorder[TAR, TEvent]] =
      hasActionHandlers.getCreatingHandler(action).map(_.asInstanceOf[CreatingActionHandler[TAR, TEvent, CreatorCommandAction]]).fold(
        fail => AlmFuture.failed(fail),
        succ => succ(action, theAlmhirt))

    def executeMutatingHandler(action: MutatorCommandAction { type Event = TEvent; type AR = TAR }, ar: TAR)(implicit theAlmhirt: Almhirt): AlmFuture[UpdateRecorder[TAR, TEvent]] =
      hasActionHandlers.getMutatingHandler(action).map(_.asInstanceOf[MutatingActionHandler[TAR, TEvent, MutatorCommandAction]]).fold(
        fail => AlmFuture.failed(fail),
        succ => succ(action, ar, theAlmhirt))

    def executeMutatingHandlers(actions: List[MutatorCommandAction { type Event = TEvent; type AR = TAR }]): AlmFuture[UpdateRecorder[TAR, TEvent]] = {
      val actionsAndHandlersVs = actions.map(x =>
        hasActionHandlers.getMutatingHandler(x).map(h =>
          (x, h.asInstanceOf[MutatingActionHandler[TAR, TEvent, MutatorCommandAction]])))
      val xx = actionsAndHandlersVs.sequence
      ???
    }

  }
}