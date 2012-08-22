package almhirt.domain

import scalaz._, Scalaz._
import org.specs2.mutable._
import almhirt.validation.{Problem}
import almhirt.validation.AlmValidation._

class UpdateSpecsWithPerson extends Specification {
  val person = TestPerson("Peter").result.forceResult
  
  "A just created person with a valid name having her name changed" should {
    "return no error when name is replaced with a non empty string" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).isAccepted
    }
    "have her name changed to the name given as a parameter" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).result.forceResult.name must beEqualTo("Bob")
    }
    "have a version of 2" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).result.forceResult.version must beEqualTo(2)
    }
    "have created a single event " in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).events.length must beEqualTo(1)
    }
    "have created a single event with a targetted version of 1" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).events.head.entityVersion must beEqualTo(1)
    }
    "have created a single event of type TestPersonNameChanged" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    "have created a TestPersonNameChanged event containing the name Bob" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].newName must beEqualTo("Bob")
    }
    "have created a TestPersonNameChanged event containing a version of 1" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].entityVersion must beEqualTo(1)
    }
    "have created a TestPersonNameChanged event containing an entityId which is the same as the origins" in {
      (Update.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].entityId must beEqualTo(person.id)
    }
    "reject having her name changed to an empty String" in {
      (Update.startWith(person) flatMap {_.changeName("")}).isRejected
    }
    "create no event when having her name changed to an empty String" in {
      (Update.startWith(person) flatMap {_.changeName("")}).events must beEmpty
    }
  }
  
  val updateWithName = Update.startWith(person) flatMap {_.changeName("Bob")}
  
  "An Update(Person),  containing a succesful update of a name having the address set" should {
    "return no error when address is replaced with a non empty string" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).isAccepted
    }
    "still have the original name" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.name must beEqualTo("Bob")
    }
    "have the address set to the new value" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.address must beEqualTo(Some("Gibraltar"))
    }
    "have a version of 3" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.version must beEqualTo(3)
    }
    "have created 2 events" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.length must beEqualTo(2)
    }
    "have have the first element of type TestPersonNameChanged" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    "have have the second element of events of type  TestPersonAddressAquired" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.tail.head.isInstanceOf[TestPersonAddressAquired]
    }
    "have created a TestPersonAddressAquired event containing the set event" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].aquiredAddress must beEqualTo("Gibraltar")
    }
    "have created a TestPersonAddressAquired event containing a version of 2" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].entityVersion must beEqualTo(2)
    }
    "have created a TestPersonAddressAquired event containing an entityId which is the same as the origins" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].entityId must beEqualTo(person.id)
    }
    "reject having her address changed to an empty String" in {
      (updateWithName flatMap {_.addressAquired("")}).isRejected
    }
    "contain the previous event when having her address changed to an empty String" in {
      (updateWithName flatMap {_.addressAquired("")}).events.length must beEqualTo(1)
    }
    "reject having her address set twice(even though to a different one" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")} flatMap{_.addressAquired("Norway")}).isRejected
    }
  }
  val updateWithNameAndAddress = Update.startWith(person) flatMap {_.changeName("Bob")} flatMap {_.addressAquired("Gibraltar")}
  
  "An Update(Person),  containing a succesful update of a name and address being moved" should {
    "return no error when address is replaced with a non empty string" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).isAccepted
    }
    "still have the original name" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).result.forceResult.name must beEqualTo("Bob")
    }
    "have the new address set" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).result.forceResult.address must beEqualTo(Some("Norway"))
    }
    "have created 3 events" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.length must beEqualTo(3)
    }
    "have have the first element of type TestPersonNameChanged" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    "have have the second element of events of type  TestPersonAddressAquired" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.tail.head.isInstanceOf[TestPersonAddressAquired]
    }
    "have have the third element of events of type  TestPersonMoved" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.tail.tail.head.isInstanceOf[TestPersonMoved]
    }
  }
  
  "An Update(Person), executing an unhandled action(resulting event not handled)" should {
   "be rejected" in {
      (updateWithNameAndAddress flatMap {_.unhandableAction()}).isRejected
    }
  }
}