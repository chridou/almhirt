package almhirt.common

trait HasExecutionContext {
  def executionContext: scala.concurrent.ExecutionContext
}

object HasExecutionContext {
  import java.util.concurrent._
  def apply(): HasExecutionContext = cached

  def cached(): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  }
  
  def single(): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  }
  
  def fixed(nThreads: Int): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newFixedThreadPool(nThreads))
  }
}