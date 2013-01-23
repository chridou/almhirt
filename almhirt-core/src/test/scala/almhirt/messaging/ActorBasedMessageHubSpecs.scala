package almhirt.messaging

import scala.concurrent.duration._
import akka.pattern._
import almhirt.syntax.almvalidation._
import almhirt._
import almhirt.almfuture.all._
import almhirt.environment.AlmhirtsystemTestkit
import scalaz._, Scalaz._
import almhirt.environment.AlmhirtSystem
import org.specs2.mutable._

class ActorBasedMessageHubSpecs extends Specification with AlmhirtsystemTestkit {
  implicit val atMost = Duration(1, "s")
  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)

  implicit def randUUID = java.util.UUID.randomUUID
  private def getHub(context: AlmhirtSystem): MessageHub = {
    MessageHub("testHub")(context)
  }

  """A MessageHub""" should {
    """accept a message""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        hub.broadcast(Message(new B(1, "B")))
        true
      }
    }
    """be able to create a global channel""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[AnyRef]("testChannel")
        true
      }
    }
    """be able to create a channel""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[AnyRef]("testChannel")
        true
      }
    }
  }

  """A MessageHub with a created global channel of payload type AnyRef""" should {
    """trigger a handler on the created channel""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        val channel = (hub.createMessageChannel[AnyRef]("testChannel")).awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* { x => hit = true }).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message(new A(1)))
        subscription.dispose()
        hit === true
      }
    }
  }
  """A MessageHub with a created global channel of payload type String using the MessageHub trait""" should {
    """trigger a handler on the created channel when a String is broadcasted""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String]("testChannel").awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message("A"))
        subscription.dispose()
        hit === true
      }
    }
    """not trigger a handler on the created channel when a UUID is broadcasted""" in {
      inTestSystem { implicit ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String]("testChannel").awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message(java.util.UUID.randomUUID))
        subscription.dispose()
        hit === false
      }
    }
  }

  """A MessageHub with a created channel with no topic of payload type AnyRef""" should {
    """trigger a handler on the created channel""" in {
      inTestSystem { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[AnyRef]("testChannel").awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true)).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message(new A(1)))
        subscription.dispose()
        hit === true
      }
    }
  }
  """A MessageHub with a created channel with no topic of payload type String""" should {
    """trigger a handler on the created channel when a String is broadcasted""" in {
      inTestSystem { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String]("testChannel").awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message("A"))
        subscription.dispose()
        hit === true
      }
    }
    """not be trigger a handler on the created channel when a UUID is broadcasted""" in {
      inTestSystem { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String]("testChannel").awaitResult(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).awaitResult(Duration.Inf).forceResult
        hub.broadcast(Message(java.util.UUID.randomUUID))
        subscription.dispose()
        hit === false
      }
    }
  }

  """A MessageHub with a created global channel of payload type String using actors directly""" should {
    """trigger a handler on the created channel when a String is broadcasted""" in {
      inTestSystem { ctx =>
        implicit val executionContext = ctx.executionContext
        val hub = getHub(ctx)
        val channel =
          (hub.actor ? CreateSubChannelQry("testChannel", MessagePredicate[String]))(atMost)
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
    }
    """not trigger a handler on the created channel when a UUID is broadcasted""" in {
      inTestSystem { ctx =>
        implicit val executionContext = ctx.executionContext
        val hub = getHub(ctx)
        val channel =
          (hub.actor ? CreateSubChannelQry("testChannel", MessagePredicate[String]))(atMost)
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
  }

}