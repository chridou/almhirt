package almhirt.akkax

import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import akka.testkit._
import org.scalatest._
import java.util.concurrent.atomic.AtomicInteger

class ActorRetryFutureTests(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("ActorRetryFutureTests"))

  protected implicit val ContextSchedulerSchedulingMagnet = new almhirt.almfuture.ActionSchedulingMagnet[Scheduler] {
    def schedule(to: Scheduler, actionBlock: ⇒ Unit, in: scala.concurrent.duration.FiniteDuration, executor: scala.concurrent.ExecutionContext): Unit = {
      to.scheduleOnce(in) { actionBlock }(executor)
    }
  }

  val scheduler = system.scheduler
  implicit val executor = system.dispatchers.defaultGlobalDispatcher

  val defaultProblem = UnspecifiedProblem("Fail!")

  test("should succeed on an immediate success with no retries and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.successful(1))
    res.awaitResultOrEscalate(1.second) should equal(1)
  }

  test("should fail on an immediate failure with no retries and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.failed[Int](defaultProblem))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should fail on an immediate failure with 1 retry and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(1), delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.failed[Int](defaultProblem))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should succeed on an immediate success with no retries and a delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.successful(1))
    res.awaitResultOrEscalate(1.second) should equal(1)
  }

  test("should fail on an immediate failure with no retries and no delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.failed[Int](defaultProblem))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should fail on an immediate failure with 1 retry and no delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(1), delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.failed[Int](defaultProblem))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should succeed on future success with no retries and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.compute(1))
    res.awaitResultOrEscalate(1.second) should equal(1)
  }

  test("should fail on an future failure with no retries and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int](defaultProblem.failure))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should fail on an future failure with 1 retry and no delay") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(1), delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int](defaultProblem.failure))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should succeed on an future success with no retries and a delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture.compute(1))
    res.awaitResultOrEscalate(1.second) should equal(1)
  }

  test("should fail on an future failure with no retries and no delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.NoRetry, delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int](defaultProblem.failure))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should fail on an future failure with 1 retry and no delay of 100 milliseconds") {
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(1), delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int](defaultProblem.failure))
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
  }

  test("should retry a future that always fails with 3 retries and no delay 4 times") {
    val timesExecuted = new AtomicInteger(0)
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(4), delay = RetryDelayMode.NoDelay)
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int] {
      val v = timesExecuted.incrementAndGet()
      info("times executed: " + v.toString)
      defaultProblem.failure
    })
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
    timesExecuted.get should equal(4)
  }

  test("should retry a future that always fails with 3 retries and a delay 4 times") {
    val timesExecuted = new AtomicInteger(0)
    val settings = RetryPolicy(numberOfRetries = NumberOfRetries.LimitedRetries(4), delay = RetryDelayMode.ConstantDelay(100.millis))
    val res = AlmFuture.retry(settings, scheduler)(AlmFuture[Int] {
      val v = timesExecuted.incrementAndGet()
      info("times executed: " + v.toString)
      defaultProblem.failure
    })
    res.awaitResult(1.second) should equal(defaultProblem.failure[Int])
    timesExecuted.get should equal(4)
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}