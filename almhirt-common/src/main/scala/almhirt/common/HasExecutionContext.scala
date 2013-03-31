package almhirt.common

import scala.language.implicitConversions

import java.util.concurrent._
import scala.concurrent.ExecutionContext

trait HasExecutionContext {
  def executionContext: scala.concurrent.ExecutionContext
  def execute(action: => Unit) {
    executionContext.execute(new Runnable {
      def run() { action }
    })
  }
  def compute[T](compute: => AlmValidation[T]): AlmFuture[T] = AlmFuture { compute }(this.executionContext)
  def computeSafely[T](compute: => AlmValidation[T]): AlmFuture[T] =
    AlmFuture { almhirt.almvalidation.funs.computeSafely(compute) }(this.executionContext)
  def computFromExceptional[T](compute: => T): AlmFuture[T] =
    AlmFuture { almhirt.almvalidation.funs.inTryCatch(compute) }(this.executionContext)
}

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

  def apply(execContext: ExecutionContext): HasExecutionContext =
    new HasExecutionContext { val executionContext = execContext }

  def cached(): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  }

  def single(): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  }

  def fixed(nThreads: Int): HasExecutionContext = new HasExecutionContext {
    val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newFixedThreadPool(nThreads))
  }

  implicit def hasExecutionContext2ExecutionContext(hasExecutionContext: HasExecutionContext): ExecutionContext = hasExecutionContext.executionContext
  implicit def executionContext2HasExecutionContext(executionContext: ExecutionContext): HasExecutionContext = HasExecutionContext(executionContext)
}