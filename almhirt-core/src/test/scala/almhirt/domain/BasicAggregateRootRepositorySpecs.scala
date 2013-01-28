//package almhirt.domain
//
//import org.scalatest._
//import org.scalatest.matchers.ShouldMatchers
//import scalaz.NonEmptyList
//import almhirt._
//import almhirt.almvalidation.kit._
//import almhirt.eventlog.DomainEventLog
//import test._
//import almhirt.environment.AlmhirtTestKit
//import scala.concurrent.duration.FiniteDuration
//import almhirt.environment.AlmhirtForExtendedTesting
//import almhirt.environment.ShutDown
//
//class BasicAggregateRootRepositorySpecs extends FlatSpec with ShouldMatchers with BeforeAndAfter with AlmhirtTestKit {
//  val shouldBe1 = TestPerson("Jim") flatMap {_.changeName("Fritz")} flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
//  val events1 = shouldBe1.events
//  val person1 = shouldBe1.result.forceResult
//
//  val shouldBe2 = TestPerson("Tom") flatMap {_.changeName("Moritz")} flatMap {_.addressAquired("Berlin")}
//  val events2 = shouldBe2.events
//  val person2 = shouldBe2.result.forceResult
//  
//  val shouldBe3 = TestPerson("Mike") flatMap {_.changeName("Michael")}
//  val events3 = shouldBe3.events
//  val person3 = shouldBe2.result.forceResult
//
//  private[this] var theAlmhirt: AlmhirtForExtendedTesting = null
//  private[this] var shutDown: ShutDown = null
//  implicit val atMost = FiniteDuration(1, "s")
//
//  override def before {
//    val (almhirt, shutdown) = createExtendedTestAlmhirt().forceResult
//    this.theAlmhirt = almhirt
//    this.shutDown = shutdown
//  }
//
//  override def after {
//    shutDown.shutDown
//  }
//  
//  def withNewBlockingRepository[T](repoComputation: AggregateRootRepository[TestPerson, TestPersonEvent] => T) = {
//    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, theAlmhirt.eventLog.actor)(theAlmhirt)
//    repoComputation(personRepository)
//  }
//  
//  
//  "A blocking PersonRepository" should 
//    "be able to store a person" in {
//      withNewBlockingRepository(implicit repo => {
//        repo.store(person1, events1).awaitResult(Duration.Inf).isSuccess
//      })}
//   it should "return the same person as stored when get was called" in {
//      withNewBlockingRepository(repo => {
//        repo.storeAndRetrieveUpdated(person1, events1).awaitResult(Duration.Inf)
//        val res = repo.get(person1.id).awaitResult(Duration.Inf)
//        res.forceResult === person1
//      })
//    }
//    it should "return a NotFoundProblem when it is queried with a wrong id" in {
//      withNewBlockingRepository(repo => {
//        repo.storeAndRetrieveUpdated(person1, events1).awaitResult(Duration.Inf)
//        val res = repo.get(person2.id).awaitResult(Duration.Inf)
//        classOf[NotFoundProblem].isAssignableFrom(res.forceProblem.getClass)
//      })
//    }
//    it should "return the correct persons when queried by id" in {
//      withNewBlockingRepository(repo => {
//        repo.storeAndRetrieveUpdated(person1, events1).awaitResult(Duration.Inf)
//        repo.storeAndRetrieveUpdated(person2, events2).awaitResult(Duration.Inf)
//        repo.storeAndRetrieveUpdated(person3, events3).awaitResult(Duration.Inf)
//        val p1 = repo.get(person1.id).awaitResult(Duration.Inf).forceResult
//        val p2 = repo.get(person2.id).awaitResult(Duration.Inf).forceResult
//        val p3 = repo.get(person3.id).awaitResult(Duration.Inf).forceResult
//        p1 === person1 && p2 === person2 && p3 === person3
//      })
//    }
//}