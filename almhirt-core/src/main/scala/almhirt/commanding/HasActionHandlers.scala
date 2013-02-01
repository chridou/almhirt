package almhirt.commanding

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.domain._

trait HasCommandActionHandlers[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def addCreatingActionHandler[TAction <: CreatorCommandAction](handler: CreatingActionHandler[AR, Event, TAction])(implicit tag: ClassTag[TAction])
  def addMutatingActionHandler[TAction <: MutatorCommandAction](handler: MutatingActionHandler[AR, Event, TAction])(implicit tag: ClassTag[TAction])

  def getCreatingHandler(action: CreatorCommandAction): AlmValidation[CreatingActionHandler[AR, Event, _]]
  def getMutationgHandler(action: MutatorCommandAction): AlmValidation[MutatingActionHandler[AR, Event, _]]
}

object HasCommandActionHandlers {
  implicit class HasCommandActionHandlersOps[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](hasActionHandlers: HasCommandActionHandlers[AR, Event]) {
    def executeCreatingHandler(action: CreatorCommandAction)(implicit theAlmhirt: Almhirt): AlmValidation[UpdateRecorder[AR, Event]] =
      for {
        handler <- hasActionHandlers.getCreatingHandler(action)
      } yield handler(action, theAlmhirt)
  }
}