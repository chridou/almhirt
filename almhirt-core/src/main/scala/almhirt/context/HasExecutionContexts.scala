package almhirt.context

import scala.concurrent.ExecutionContext

trait HasExecutionContexts {
  def futuresContext: ExecutionContext
  def crunchersContext: ExecutionContext
  def blockersContext: ExecutionContext
}