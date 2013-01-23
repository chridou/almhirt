package almhirt.messaging

import org.scalatest._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import akka.pattern._
import almhirt.syntax.almvalidation._
import almhirt.almfuture.all._
import almhirt.environment._

class ActorBasedMessageHubTests extends FunSuite with BeforeAndAfterAll with AlmhirtsystemTestkit {
  implicit val system = createTestSystem()
  implicit val executionContext = system.executionContext
  implicit val atMost = FiniteDuration(1, "s")
  implicit def getUUID = java.util.UUID.randomUUID()

  override def afterAll {
    system.dispose()
  }

  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)

  private def getHub: MessageHub = MessageHub(java.util.UUID.randomUUID().toString())

  test("A MessageHub must accept a message") {
    val hub = getHub
    hub.broadcast(Message(new B(1, "B")))
    hub.close
  }

  test("A MessageHub must be able to create a global channel") {
    val hub = getHub
    val channel = hub.createMessageChannel[AnyRef]("testChannel")
    hub.close
  }

  test("A MessageHub must be able to create a channel") {
    val hub = getHub
    val channel = hub.createMessageChannel[AnyRef]("testChannel")
    hub.close
  }

  test("""A MessageHub with a created global channel of payload type AnyRef must trigger a handler on the created channel""") {
    val hub = getHub
    val channel = (hub.createMessageChannel[AnyRef]("testChannel" + system.getUuid.toString())).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* { x => hit = true }).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message(new A(1)))
    subscription.dispose()
    hit === true
  }

  test("""A MessageHub with a created global channel of payload type String must trigger a handler on the created channel when a String is broadcasted""") {
    val hub = getHub
    val channel = hub.createMessageChannel[String]("testChannel" + system.getUuid.toString()).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message("A"))
    subscription.dispose()
    hit === true
  }

  test("""A MessageHub with a created global channel of payload type String must not trigger a handler on the created channel when a UUID is broadcasted""") {
    val hub = getHub
    val channel = hub.createMessageChannel[String]("testChannel" + system.getUuid.toString()).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message(java.util.UUID.randomUUID))
    subscription.dispose()
    hit === false
  }

  test("""A MessageHub with a created channel with no topic of payload type AnyRef must trigger a handler on the created channel""") {
    val hub = getHub
    val channel = hub.createMessageChannel[AnyRef]("testChannel" + system.getUuid.toString()).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* (x => hit = true)).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message(new A(1)))
    subscription.dispose()
    hit === true
  }

  test("""A MessageHub with a created channel with no topic of payload type String must trigger a handler on the created channel when a String is broadcasted""") {
    val hub = getHub
    val channel = hub.createMessageChannel[String]("testChannel" + system.getUuid.toString()).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message("A"))
    subscription.dispose()
    hit === true
  }

  test("""A MessageHub with a created channel with no topic of payload type String must not be trigger a handler on the created channel when a UUID is broadcasted""") {
    val hub = getHub
    val channel = hub.createMessageChannel[String]("testChannel" + system.getUuid.toString()).awaitResult(Duration.Inf).forceResult
    var hit = false
    val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
    hub.broadcast(Message(java.util.UUID.randomUUID))
    subscription.dispose()
    hit === false
  }

  test("""A MessageHub with a created global channel of payload type String using actors directly must trigger a handler on the created channel when a String is broadcasted""") {
    val hub = getHub
    val channel =
      (hub.actor ? CreateSubChannelQry("testChannel" + system.getUuid.toString(), MessagePredicate[String]))(atMost)
        .mapTo[NewSubChannelRsp]
        .map(_.channel)
        .toAlmFuture
        .awaitResult
        .forceResult
    var hit = false
    val subscription = (channel ? SubscribeQry(MessagingSubscription.typeBasedHandler[AnyRef](anyRef => hit = true)))(atMost)
      .mapTo[SubscriptionRsp]
      .map(_.registration)
      .toAlmFuture
      .awaitResult
      .forceResult
    hub.broadcast(Message("A"))

    hit === true
  }

  test("""A MessageHub with a created global channel of payload type String using actors directly must not trigger a handler on the created channel when a UUID is broadcasted""") {
    val hub = getHub
    val channel =
      (hub.actor ? CreateSubChannelQry("testChannel" + system.getUuid.toString(), MessagePredicate[String]))(atMost)
        .mapTo[NewSubChannelRsp]
        .map(_.channel)
        .toAlmFuture
        .awaitResult
        .forceResult
    var hit = false
    val subscription = (channel ? SubscribeQry(MessagingSubscription.typeBasedHandler[AnyRef](anrRef => hit = true)))(atMost)
      .mapTo[SubscriptionRsp]
      .map(_.registration)
      .toAlmFuture
      .awaitResult
      .forceResult
    hub.broadcast(Message(java.util.UUID.randomUUID))
    hit === false
  }

}