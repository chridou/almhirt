package almhirt.configuration

sealed trait NumberOfRetries
object NumberOfRetries {
  object NoRetry extends NumberOfRetries
  final case class LimitedRetries(retries: Int) extends NumberOfRetries
  object InfiniteRetries extends NumberOfRetries

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
}

final case class RetrySettings2(numberOfRetries: NumberOfRetries, delay: RetryDelayMode)

