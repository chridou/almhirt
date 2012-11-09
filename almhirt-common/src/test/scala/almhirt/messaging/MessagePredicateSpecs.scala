package almhirt.messaging

import org.specs2.mutable._

class MessagePredicateSpecs extends Specification {
  "An AlwaysTrueMessagePredicate" should {
    "return true" in {
      AlwaysTrueMessagePredicate(Message.createWithUuid(java.util.UUID.randomUUID())) === true
    }
  }

  "An MessagePredacate simply checking for the type" should {
    "return true when filtering AnyRef and applied to a UUID" in {
      val predicate = MessagePredicate[AnyRef]
      predicate(Message.createWithUuid(java.util.UUID.randomUUID())) === true
    }
    "return true when filtering AnyRef and applied to a String" in {
      val predicate = MessagePredicate[AnyRef]
      predicate(Message.createWithUuid("")) === true
    }
    "return true when filtering UUID and applied to a UUID" in {
      val predicate = MessagePredicate[java.util.UUID]
      predicate(Message.createWithUuid(java.util.UUID.randomUUID())) === true
    }
    "return false when filtering UUID and applied to a String" in {
      val predicate = MessagePredicate[java.util.UUID]
      predicate(Message.createWithUuid("")) === false
    }
  }
  
}