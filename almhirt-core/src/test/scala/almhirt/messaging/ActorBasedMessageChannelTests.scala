package almhirt.messaging

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import almhirt.environment.configuration.impl._

class ActorBasedMessageChannelTests extends FunSuite with BeforeAndAfterAll with ShouldMatchers with AlmhirtTestKit {
  private[this] val (theAlmhirt, shutDown) = createTestAlmhirt(createDefaultBootStrapper()).forceResult
  implicit val atMost = FiniteDuration(1, "s")
  implicit val alm = theAlmhirt

  override def afterAll {
    shutDown.shutDown
  }

  private def createChannel[T <: AnyRef](implicit tag: ClassTag[T]): MessageChannel[T] = MessageChannel[T](java.util.UUID.randomUUID().toString(), None)

  test("A MessageChannel creates creates a subscription") {
    val channel = createChannel[AnyRef]
    val future = channel <-* ({ case _ => () }, _ => true)
    val subscription = future.awaitResult(Duration.Inf)
    Thread.sleep(50)
    subscription should not equal (null)
  }

  test("A MessageChannel does not execute a handler when the subscription is cancelled") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, _ => true)
    val subscription = future.awaitResult(Duration.Inf).forceResult
    subscription.dispose()
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hit should be(false)
  }

  test("A MessageChannel does only execute one handler when the subscription for one of two was cancelled") {
    val channel = createChannel[AnyRef]
    var hitCount = 0
    val future1 = channel <-* ({ case _ => hitCount += 1 }, _ => true)
    val future2 = channel <-* ({ case _ => hitCount += 2 }, _ => true)
    val subscription1 = future1.awaitResult(Duration.Inf).forceResult
    val subscription2 = future2.awaitResult(Duration.Inf).forceResult
    subscription1.dispose()
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hitCount should equal(2)
  }

  test("A MessageChannel does execute a subscribed handler when classifier always returns true") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, _ => true)
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hit should be(true)
  }

  test("A MessageChannel does execute two handlers when classifier always returns true for 2 subscriptions") {
    val channel = createChannel[AnyRef]
    var hitCount = 0
    val future1 = channel <-* ({ case _ => hitCount += 1 }, _ => false)
    val future2 = channel <-* ({ case _ => hitCount += 2 }, _ => true)
    val subscription1 = future1.awaitResult(Duration.Inf).forceResult
    val subscription2 = future2.awaitResult(Duration.Inf).forceResult
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hitCount should equal(2)
  }

  test("A MessageChannel does not execute a subscribed handler when classifier always returns false") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, _ => false)
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hit should be(false)
  }

  test("A MessageChannel does execute a subscribed handler when classifier is met") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true })
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hit should be(true)
  }

  test("A MessageChannel does not execute a subscribed handler when classifier is not met but the payload is of the same type") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true; case _ => false })
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message("b"))
    channel.close()
    Thread.sleep(50)
    hit should be(false)
  }

  test("A MessageChannel does not execute a subscribed handler when classifier is not met because the payload is of a different type") {
    val channel = createChannel[AnyRef]
    var hit = false
    val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "1" => true; case _ => false })
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message("a"))
    channel.close()
    Thread.sleep(50)
    hit should be(false)
  }

}