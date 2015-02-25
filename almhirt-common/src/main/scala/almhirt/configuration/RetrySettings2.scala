package almhirt.configuration

import javax.xml.datatype.Duration

sealed trait NumberOfRetries
object NumberOfRetries {
  object NoRetry extends NumberOfRetries
  final case class LimitedRetries(retries: Int) extends NumberOfRetries
  object InfiniteRetries extends NumberOfRetries

  def apply(n: Int): NumberOfRetries =
    if (n <= 1) NoRetry
    else LimitedRetries(n)

  implicit class NumberOfRetriesOps(val self: NumberOfRetries) extends AnyVal {
    def hasRetriesLeft: Boolean = {
      self match {
        case NoRetry                 ⇒ false
        case LimitedRetries(retries) ⇒ retries > 0
        case InfiniteRetries         ⇒ true
      }
    }

    def oneLess: NumberOfRetries = {
      self match {
        case NoRetry ⇒
          NoRetry
        case LimitedRetries(retries) ⇒
          if (retries > 0)
            LimitedRetries(retries - 1)
          else
            NoRetry
        case InfiniteRetries ⇒
          InfiniteRetries
      }
    }
  }
}

sealed trait RetryDelayMode {
  def calculator: RetryDelayCalculator
}

sealed trait RetryDelayCalculator {
  def next: (scala.concurrent.duration.FiniteDuration, RetryDelayCalculator)
}

object RetryDelayMode {
  object NoDelay extends RetryDelayMode {
    override def calculator: RetryDelayCalculator = new RetryDelayCalculator {
      def next: (scala.concurrent.duration.FiniteDuration, RetryDelayCalculator) =
        (scala.concurrent.duration.Duration.Zero, this)
    }

  }

  final case class ConstantDelay(duration: scala.concurrent.duration.FiniteDuration) extends RetryDelayMode {
    override def calculator: RetryDelayCalculator = new RetryDelayCalculator {
      def next: (scala.concurrent.duration.FiniteDuration, RetryDelayCalculator) =
        (duration, this)
    }
  }

  def apply(duration: scala.concurrent.duration.Duration): RetryDelayMode =
    if (duration.isFinite()) {
      if (duration <= scala.concurrent.duration.Duration.Zero)
        NoDelay
      else
        ConstantDelay(scala.concurrent.duration.Duration.apply(duration.toNanos, scala.concurrent.duration.NANOSECONDS))
    } else {
      if (duration == scala.concurrent.duration.Duration.MinusInf)
        NoDelay
      else
        ConstantDelay(scala.concurrent.duration.Duration.apply(Int.MaxValue, scala.concurrent.duration.HOURS))
    }
}

final case class RetrySettings2(numberOfRetries: NumberOfRetries, delay: RetryDelayMode)

