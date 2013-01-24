package almhirt.common

trait HasExecutionContext {
  def executionContext: scala.concurrent.ExecutionContext
}

import java.util.concurrent._

trait HasCachedExecutionContext extends HasExecutionContext {
  val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
}

trait HasSingleThreadedExecutionContext extends HasExecutionContext {
  val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
}

trait HasFixedSizeExecutionContext extends HasExecutionContext {
  def nThreads: Int
  val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newFixedThreadPool(nThreads))
}

object HasExecutionContext {
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