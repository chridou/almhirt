package almhirt.domain

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scalaz._, Scalaz._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.core.test._

class UpdateSpecsWithPerson extends FlatSpec with ShouldMatchers {
  val person = TestPerson("Peter").result.forceResult
  
  "A just created person with a valid name having her name changed" should 
    "return no error when name is replaced with a non empty string" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).isAccepted
    }
    it should "have her name changed to the name given as a parameter" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).result.forceResult.name should equal("Bob")
    }
    it should "have a version of 2" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).result.forceResult.version should equal(2)
    }
    it should "have created a single event " in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).events.length should equal(1)
    }
    it should "have created a single event with a targetted version of 1" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).events.head.aggVersion should equal(1)
    }
    it should "have created a single event of type TestPersonNameChanged" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    it should "have created a TestPersonNameChanged event containing the name Bob" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].newName should equal("Bob")
    }
    it should "have created a TestPersonNameChanged event containing a version of 1" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].aggVersion should equal(1)
    }
    it should "have created a TestPersonNameChanged event containing an entityId which is the same as the origins" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")})
        .events.head.asInstanceOf[TestPersonNameChanged].aggId should equal(person.id)
    }
    it should "reject having her name changed to an empty String" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("")}).isRejected
    }
    it should "create no event when having her name changed to an empty String" in {
      (UpdateRecorder.startWith(person) flatMap {_.changeName("")}).events should be ('empty)
    }
  
  val updateWithName = UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")}
  
  "An Update(Person),  containing a succesful update of a name having the address set" should 
    "return no error when address is replaced with a non empty string" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).isAccepted
    }
    it should "still have the original name" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.name should equal("Bob")
    }
    it should "have the address set to the new value" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.address should equal(Some("Gibraltar"))
    }
    it should "have a version of 3" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).result.forceResult.version should equal(3)
    }
    it should "have created 2 events" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.length should equal(2)
    }
    it should "have have the first element of type TestPersonNameChanged" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    it should "have have the second element of events of type  TestPersonAddressAquired" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")}).events.tail.head.isInstanceOf[TestPersonAddressAquired]
    }
    it should "have created a TestPersonAddressAquired event containing the set event" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].aquiredAddress should equal("Gibraltar")
    }
    it should "have created a TestPersonAddressAquired event containing a version of 2" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].aggVersion should equal(2)
    }
    it should "have created a TestPersonAddressAquired event containing an entityId which is the same as the origins" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")})
        .events.tail.head.asInstanceOf[TestPersonAddressAquired].aggId should equal(person.id)
    }
    it should "reject having her address changed to an empty String" in {
      (updateWithName flatMap {_.addressAquired("")}).isRejected
    }
    it should "contain the previous event when having her address changed to an empty String" in {
      (updateWithName flatMap {_.addressAquired("")}).events.length should equal(1)
    }
    it should "reject having her address set twice(even though to a different one" in {
      (updateWithName flatMap {_.addressAquired("Gibraltar")} flatMap{_.addressAquired("Norway")}).isRejected
    }

  val updateWithNameAndAddress = UpdateRecorder.startWith(person) flatMap {_.changeName("Bob")} flatMap {_.addressAquired("Gibraltar")}
  
  "An Update(Person),  containing a succesful update of a name and address being moved" should 
    "return no error when address is replaced with a non empty string" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).isAccepted
    }
    it should "still have the original name" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).result.forceResult.name should equal("Bob")
    }
    it should "have the new address set" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).result.forceResult.address should equal(Some("Norway"))
    }
    it should "have created 3 events" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.length should equal(3)
    }
    it should "have have the first element of type TestPersonNameChanged" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.head.isInstanceOf[TestPersonNameChanged]
    }
    it should "have have the second element of events of type  TestPersonAddressAquired" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.tail.head.isInstanceOf[TestPersonAddressAquired]
    }
    it should "have have the third element of events of type  TestPersonMoved" in {
      (updateWithNameAndAddress flatMap {_.move("Norway")}).events.tail.tail.head.isInstanceOf[TestPersonMoved]
    }
  
  "An Update(Person), executing an unhandled action(resulting event not handled)" should 
   "be rejected" in {
      (updateWithNameAndAddress flatMap {_.unhandableAction()}).isRejected
    }
}