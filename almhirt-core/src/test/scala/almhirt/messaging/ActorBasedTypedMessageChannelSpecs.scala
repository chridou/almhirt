package almhirt.messaging

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.environment._

class ActorBasedTypedMessageChannelTests extends FlatSpec with ShouldMatchers with BeforeAndAfterAll with AlmhirtTestKit {
  private[this] val bootstrapper = createDefaultBootStrapper()
  private[this] val (theAlmhirt, shutDown) = createTestAlmhirt(bootstrapper).forceResult
  implicit val atMost = FiniteDuration(1, "s")
  implicit val alm = theAlmhirt
  implicit val executionContext = theAlmhirt.executionContext

  override def afterAll {
    shutDown.shutDown
  }

  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)

  private def getChannel[T <: AnyRef](implicit tag: ClassTag[T]): MessageChannel[T] = MessageChannel[T](java.util.UUID.randomUUID().toString())

  """A MessageChannel[A] where B <: A""" should
    """accept both As and Bs as message payloads""" in {
      val channel = getChannel[A]
      channel.post(Message(new A(1)))
      channel.post(Message(new B(1, "B")))
      channel.close()
    }

  it should """take a handler for A that will be triggered when an A is posted""" in {
    val channel = getChannel[A]
    var hitA = false
    val future = channel <-* (x => hitA = true)
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message(new A(1)))
    subscription.dispose()
    channel.close()
    Thread.sleep(50)
    hitA should be(true)
  }

  it should """take a handler for A that will be triggered when an B is posted""" in {
    val channel = getChannel[A]
    var hitA = false
    val future = channel <-* (x => hitA = true)
    val subscription = future.awaitResult(Duration.Inf).forceResult
    channel.post(Message(new B(1, "A")))
    subscription.dispose()
    channel.close()
    Thread.sleep(50)
    hitA should be(true)
  }

  """A MessageChannel[B] where B <: A""" should
    """take a handler for B that will be triggered when a B is posted""" in {
      val channel = getChannel[A]
      var hitB = false
      val future = channel <-* (x => hitB = true)
      val subscription = future.awaitResult(Duration.Inf).forceResult
      channel.post(Message(new B(1, "A")))
      subscription.dispose()
      channel.close()
      Thread.sleep(50)
      hitB should be(true)
    }

  """A MessageChannel[A] with a subchannel of MessageChannel[B] where B <: A""" should
    """trigger the handler on the parent for an A posted on the parent and not the handler on the subchannel""" in {
      val channel = getChannel[A]
      val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
      var hitA = false
      var hitB = false
      val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
      val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
      channel.post(Message(new A(1)))
      subscriptionA.dispose()
      subscriptionB.dispose()
      channel.close()
      subChannel.close()
      Thread.sleep(50)
      hitA should be(true)
      hitB should be(false)
    }

  it should """trigger only the handler on the subchannel for a B posted on the subchannel""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
    subChannel.post(Message(new B(1, "B")))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitA should be(false)
    hitB should be(true)
  }

  it should """trigger the handler on the parent for an A posted on the parent""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new A(1)))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitA should be(true)
  }

  it should """not trigger the handler on the subchannel for an A posted on the parent""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new A(1)))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitB should be(false)
  }

  it should """not trigger the handler on the parent for an A posted on the parent when the classifier is not met""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new A(1)))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitA should be(false)
  }

  it should """trigger the handler on the parent for an A posted on the parent when the classifier is met""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new A(1)))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitA should be(true)
  }

  it should """not trigger the handler on the parent or the subchannelfor an B posted on the parent when the classifier is not met""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new B(1, "B")))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitA should be(false)
    hitB should be(false)
  }

  it should """trigger the handler on the parent and the subchannel for an B posted on the parent when the classifier is met""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new B(1, "B")))
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    subscriptionA.dispose()
    subscriptionB.dispose()
    hitA should be(true)
    hitB should be(true)
  }

  it should """trigger the handler on the parent and not on the subchannel for an B posted on the parent when the classifier is met only in the parent""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new B(1, "B")))
    subscriptionA.dispose()
    subscriptionB.dispose()
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    hitB should be(false)
  }

  it should """not trigger the handler on the parent but on the subchannel for an B posted on the subchannel when the classifier is met only in the subchannel""" in {
    val channel = getChannel[A]
    val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
    var hitA = false
    var hitB = false
    val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
    val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).awaitResult(Duration.Inf).forceResult
    channel.post(Message(new B(1, "B")))
    channel.close()
    subChannel.close()
    Thread.sleep(50)
    subscriptionA.dispose()
    subscriptionB.dispose()
    hitA should be(false)
    hitB should be(true)
  }
}