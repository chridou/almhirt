package almhirt.common

import scala.annotation.implicitNotFound

trait HasExecutionContext {
  def executionContext: scala.concurrent.ExecutionContext
}