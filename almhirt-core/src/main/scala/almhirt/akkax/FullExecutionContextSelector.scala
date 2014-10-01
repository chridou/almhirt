package almhirt.akkax

import scala.concurrent.ExecutionContext
import akka.actor.ActorContext
import almhirt.common._

sealed trait ExtendedExecutionContextSelector {
  def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext
}

object ExtendedExecutionContextSelector {
  case object SelectFuturesContext extends ExtendedExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.futuresContext
  }
  case object SelectBlockersContext extends ExtendedExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.blockersContext
  }
  case object SelectCrunchersContext extends ExtendedExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.crunchersContext
  }
  case object SelectDefaultGlobalDispatcher extends ExtendedExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.system.dispatchers.defaultGlobalDispatcher
  }
  case object SelectActorDispatcher extends ExtendedExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.dispatcher
  }

  implicit class ExtendedExecutionContextSelectorOps(self: ExtendedExecutionContextSelector) {
    def toParsableString: String =
      self match {
        case SelectDefaultGlobalDispatcher ⇒ "default-global-dispatcher"
        case SelectActorDispatcher ⇒ "actor-dispatcher"
        case SelectFuturesContext ⇒ "futures-context"
        case SelectBlockersContext ⇒ "blockers-context"
        case SelectCrunchersContext ⇒ "crunchers-context"
      }
  }

  def parseString(str: String): AlmValidation[ExtendedExecutionContextSelector] = {
    import scalaz.syntax.validation._
    str match {
      case "default-global-dispatcher" ⇒ SelectDefaultGlobalDispatcher.success
      case "actor-dispatcher" ⇒ SelectActorDispatcher.success
      case "futures-context" ⇒ SelectFuturesContext.success
      case "blockers-context" ⇒ SelectBlockersContext.success
      case "crunchers-context" ⇒ SelectCrunchersContext.success
      case invalid ⇒ ParsingProblem(s""""$invalid" is not a valid string representaion of an "ExtendedExecutionContextSelector".""").failure
    }
  }
}
