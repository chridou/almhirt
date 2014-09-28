package almhirt.akkax

import scala.concurrent.ExecutionContext
import akka.actor.ActorContext
import almhirt.common._

sealed trait ExecutionContextSelector {
  def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext
}

object ExecutionContextSelector {
  case object SelectDefaultGlobalDispatcher extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.system.dispatchers.defaultGlobalDispatcher
  }
  case object SelectActorDispatcher extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.dispatcher
  }
  case object SelectFuturesContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.futuresContext
  }
  case object SelectBlockersContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.blockersContext
  }
  case object SelectCrunchersContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.crunchersContext
  }

  implicit class ExecutionContextSelectorOps(self: ExecutionContextSelector) {
    def toParsableString: String =
      self match {
        case SelectDefaultGlobalDispatcher ⇒ "default-global-dispatcher"
        case SelectActorDispatcher ⇒ "actor-dispatcher"
        case SelectFuturesContext ⇒ "futures-context"
        case SelectBlockersContext ⇒ "blockers-context"
        case SelectCrunchersContext ⇒ "crunchers-context"
      }
  }

  def parseString(str: String): AlmValidation[ExecutionContextSelector] = {
    import scalaz.syntax.validation._
    str match {
      case "default-global-dispatcher" ⇒ SelectDefaultGlobalDispatcher.success
      case "actor-dispatcher" ⇒ SelectActorDispatcher.success
      case "futures-context" ⇒ SelectFuturesContext.success
      case "blockers-context" ⇒ SelectBlockersContext.success
      case "crunchers-context" ⇒ SelectCrunchersContext.success
      case invalid ⇒ ParsingProblem(s""""$invalid" is not a valid string representaion of an "ExecutionContextSelector".""").failure
    }
  }
}