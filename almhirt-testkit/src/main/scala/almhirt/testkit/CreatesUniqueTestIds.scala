package almhirt.testkit

trait CreatesUniqueTestIds {
   private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
   def nextTestId = currentTestId.getAndIncrement()
}