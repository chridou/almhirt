package almhirt.common

trait HasExecutionContext {
  def executionContext: scala.concurrent.ExecutionContext
}