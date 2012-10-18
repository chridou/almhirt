package almhirt.messaging

import org.specs2.mutable._
import akka.util.Duration
import almhirt.syntax.almvalidation._
import almhirt.almakka._
import scalaz._, Scalaz._

class ActorBasedMessageChannelSpecs extends Specification with AlmAkkaContextTestKit {
  implicit def randUUID = java.util.UUID.randomUUID
  private def getChannel[T <: AnyRef](context: AlmAkkaContext)(implicit m: Manifest[T]): MessageChannel[T] = {
    impl.ActorBasedMessageChannel[T](Some("testChannel"), context)
  }

  "An ActorBasedMessageStream" should {
    "return a subscription when subscribed to" in {
      inOwnContext { ctx =>
        val channel = getChannel[AnyRef](ctx)
        val future = channel <-* ({ case _ => () }, _ => true)
        val subscription = future.awaitResult(Duration.Inf)
        subscription must not beNull
      }
    }

    "not execute a handler when the subscription is cancelled" in {
      inOwnContext { ctx =>
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
      inOwnContext { ctx =>
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
      inOwnContext { ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, _ => true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hit
      }
    }
    "execute two handlers when classifier always returns true for 2 subscriptions" in {
      inOwnContext { ctx =>
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
      inOwnContext { ctx =>
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
      inOwnContext { ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, _ => false)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        !hit
      }
    }
    "execute a subscribed handler when classifier is met" in {
      inOwnContext { ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true })
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("a"))
        hit
      }
    }
    "not execute a subscribed handler when classifier is not met but the payload is of the same type" in {
      inOwnContext { ctx =>
        val channel = getChannel[AnyRef](ctx)
        var hit = false
        val future = channel <-* ({ case _ => hit = true }, x => x.payload match { case "a" => true; case _ => false })
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message("b"))
        !hit
      }
    }
    "not execute a subscribed handler when classifier is not met because the payload is of a different type" in {
      inOwnContext { ctx =>
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

