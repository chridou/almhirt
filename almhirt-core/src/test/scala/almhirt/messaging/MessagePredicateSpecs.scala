package almhirt.messaging

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import akka.pattern._
import almhirt.syntax.almvalidation._
import almhirt.almfuture.all._
import almhirt.environment._

class MessagePredicateSpecs extends FlatSpec with ShouldMatchers {
  "An AlwaysTrueMessagePredicate" should
    "return true" in {
      AlwaysTrueMessagePredicate(Message.create("")) should be(true)
    }

  "An MessagePredacate simply checking for the type" should
    "return true when filtering AnyRef and applied to a UUID" in {
      val predicate = MessagePredicate[AnyRef]
      predicate(Message.create(java.util.UUID.randomUUID())) should be(true)
    }
  it should "return true when filtering AnyRef and applied to a String" in {
    val predicate = MessagePredicate[AnyRef]
    predicate(Message.create("")) === true
  }
  it should "return true when filtering UUID and applied to a UUID" in {
    val predicate = MessagePredicate[java.util.UUID]
    predicate(Message.create(java.util.UUID.randomUUID())) should be(true)
  }
  it should "return false when filtering UUID and applied to a String" in {
    val predicate = MessagePredicate[java.util.UUID]
    predicate(Message.create("")) should be(false)
  }

}