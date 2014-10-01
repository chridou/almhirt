package almhirt.common

import scala.concurrent.ExecutionContext

trait HasExecutionContexts {
  def futuresContext: ExecutionContext
  def crunchersContext: ExecutionContext
  def blockersContext: ExecutionContext
}

sealed trait ExecutionContextSelector {
  def select(executionContexts: HasExecutionContexts): ExecutionContext
}

object ExecutionContextSelector {
  case object SelectFuturesContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.futuresContext
  }
  case object SelectBlockersContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.blockersContext
  }
  case object SelectCrunchersContext extends ExecutionContextSelector {
    def select(executionContexts: HasExecutionContexts): ExecutionContext =
      executionContexts.crunchersContext
  }
  
  implicit class ExecutionContextSelectorOps(self: ExecutionContextSelector) {
    def toParsableString: String =
      self match {
        case SelectFuturesContext ⇒ "futures-context"
        case SelectBlockersContext ⇒ "blockers-context"
        case SelectCrunchersContext ⇒ "crunchers-context"
      }
  }

  def parseString(str: String): AlmValidation[ExecutionContextSelector] = {
    import scalaz.syntax.validation._
    str match {
      case "futures-context" ⇒ SelectFuturesContext.success
      case "blockers-context" ⇒ SelectBlockersContext.success
      case "crunchers-context" ⇒ SelectCrunchersContext.success
      case invalid ⇒ ParsingProblem(s""""$invalid" is not a valid string representaion of an "ExecutionContextSelector".""").failure
    }
  }
}