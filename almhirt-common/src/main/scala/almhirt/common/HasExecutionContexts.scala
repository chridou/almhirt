package almhirt.common

import scala.concurrent.ExecutionContext

trait HasExecutionContexts {
  def futuresContext: ExecutionContext
  def crunchersContext: ExecutionContext
  def blockersContext: ExecutionContext
}