package almhirt.messaging

import org.specs2.mutable._
import akka.util.Duration
import almhirt.environment.AlmhirtContextTestKit
import almhirt.syntax.almvalidation._
import almhirt.environment.AlmhirtsystemTestkit
import almhirt.environment.AlmhirtSystem

class ActorBasedMessageChannelSpecs extends Specification with AlmhirtsystemTestkit {
  implicit val atMost = akka.util.Duration(1, "s")
  implicit def getUUID = java.util.UUID.randomUUID()
  private def getChannel[T <: AnyRef](context: AlmhirtSystem)(implicit m: Manifest[T]): MessageChannel[T] = {
    MessageChannel[T]("testChannel")(context, m)
  }

  "An ActorBasedMessageStream" should {
    "return a subscription when subscribed to" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        val future = channel <-* ({ case _ => () }, _ => true)
        val subscription = future.awaitResult(Duration.Inf)
        subscription must not beNull
      }
  }

    "not execute a handler when the subscription is cancelled" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, _ => true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        subscription.dispose()
        channel.post(Message("a"))
        !hit
      }
    }
    "execute one handler when the subscription for one of two was cancelled" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hitCount = 0
        val future1 = channel <-* ({ case _ => hitCount += 1 }, _ => true)
        val future2 = channel <-* ({ case _ => hitCount += 2 }, _ => true)
        val subscription1 = future1.awaitResult(Duration.Inf).forceResult
        val subscription2 = future2.awaitResult(Duration.Inf).forceResult
        subscription1.dispose()
        channel.post(Message("a"))
        hitCount must beEqualTo(2)
      }
    }
    "execute a subscribed handler when classifier always returns true" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, _ => true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hit
      }
    }
    "execute two handlers when classifier always returns true for 2 subscriptions" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hitCount = 0
        val future1 = channel <-* ({ case _ => hitCount += 1 }, _ => true)
        val future2 = channel <-* ({ case _ => hitCount += 2 }, _ => true)
        val subscription1 = future1.awaitResult(Duration.Inf).forceResult
        val subscription2 = future2.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hitCount must beEqualTo(3)
      }
    }
    "execute only the handler for which the classifier returns true" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hitCount = 0
        val future1 = channel <-* ({ case _ => hitCount += 1 }, _ => false)
        val future2 = channel <-* ({ case _ => hitCount += 2 }, _ => true)
        val subscription1 = future1.awaitResult(Duration.Inf).forceResult
        val subscription2 = future2.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hitCount must beEqualTo(2)
      }
    }
    "never execute a subscribed handler when classifier always returns false" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, _ => false)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        !hit
      }
    }
    "execute a subscribed handler when classifier is met" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true })
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hit
      }
    }
    "not execute a subscribed handler when classifier is not met but the payload is of the same type" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true; case _ => false })
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("b"))
        !hit
      }
    }
    "not execute a subscribed handler when classifier is not met because the payload is of a different type" in {
      inTestSystem { implicit ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "1" => true; case _ => false })
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        !hit
      }
    }
  }
}

