package almhirt.akkax

import scala.concurrent.ExecutionContext
import akka.actor.ActorContext
import almhirt.common._

sealed trait FullExecutionContextSelector {
  def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext
}

sealed trait MinorExecutionContextSelector {
  def select(executionContexts: HasExecutionContexts): ExecutionContext
}

object FullExecutionContextSelector {
  case object SelectFuturesContext extends FullExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.futuresContext
  }
  case object SelectBlockersContext extends FullExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.blockersContext
  }
  case object SelectCrunchersContext extends FullExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      executionContexts.crunchersContext
  }
  case object SelectDefaultGlobalDispatcher extends FullExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.system.dispatchers.defaultGlobalDispatcher
  }
  case object SelectActorDispatcher extends FullExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts, actorContext: ActorContext): ExecutionContext =
      actorContext.dispatcher
  }

  implicit class FullExecutionContextSelectorOps(self: FullExecutionContextSelector) {
    def toParsableString: String =
      self match {
        case SelectDefaultGlobalDispatcher ⇒ "default-global-dispatcher"
        case SelectActorDispatcher ⇒ "actor-dispatcher"
        case SelectFuturesContext ⇒ "futures-context"
        case SelectBlockersContext ⇒ "blockers-context"
        case SelectCrunchersContext ⇒ "crunchers-context"
      }
  }

  def parseString(str: String): AlmValidation[FullExecutionContextSelector] = {
    import scalaz.syntax.validation._
    str match {
      case "default-global-dispatcher" ⇒ SelectDefaultGlobalDispatcher.success
      case "actor-dispatcher" ⇒ SelectActorDispatcher.success
      case "futures-context" ⇒ SelectFuturesContext.success
      case "blockers-context" ⇒ SelectBlockersContext.success
      case "crunchers-context" ⇒ SelectCrunchersContext.success
      case invalid ⇒ ParsingProblem(s""""$invalid" is not a valid string representaion of an "FullExecutionContextSelector".""").failure
    }
  }
}

object MinorExecutionContextSelector {
  case object SelectFuturesContext extends MinorExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.futuresContext
  }
  case object SelectBlockersContext extends MinorExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.blockersContext
  }
  case object SelectCrunchersContext extends MinorExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.crunchersContext
  }
  
  implicit class ExecutionContextSelectorOps(self: MinorExecutionContextSelector) {
    def toParsableString: String =
      self match {
        case SelectFuturesContext ⇒ "futures-context"
        case SelectBlockersContext ⇒ "blockers-context"
        case SelectCrunchersContext ⇒ "crunchers-context"
      }
  }

  def parseString(str: String): AlmValidation[MinorExecutionContextSelector] = {
    import scalaz.syntax.validation._
    str match {
      case "futures-context" ⇒ SelectFuturesContext.success
      case "blockers-context" ⇒ SelectBlockersContext.success
      case "crunchers-context" ⇒ SelectCrunchersContext.success
      case invalid ⇒ ParsingProblem(s""""$invalid" is not a valid string representaion of an "MinorExecutionContextSelector".""").failure
    }
  }
}