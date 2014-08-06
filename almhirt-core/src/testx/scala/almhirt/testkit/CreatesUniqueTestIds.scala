package almhirt.testkit

private[testkit] object TestIdCounter {
   private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
   def nextTestId = currentTestId.getAndIncrement()
}

trait CreatesUniqueTestIds {
   def nextTestId = TestIdCounter.nextTestId
}