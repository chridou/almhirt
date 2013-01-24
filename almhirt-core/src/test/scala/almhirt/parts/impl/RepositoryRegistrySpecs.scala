package almhirt.parts.impl

import org.scalatest._
import scala.concurrent.duration._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.environment.AlmhirtsystemTestkit
import almhirt.eventlog._
import almhirt.parts.HasRepositories
import test._
import akka.testkit.TestEvent

class RepositoryRegistrySpecs extends FlatSpec with BeforeAndAfterAll with AlmhirtsystemTestkit {
  implicit val system = createTestSystem()
  implicit val atMost = FiniteDuration(1, "s")

  override def afterAll {
    system.dispose()
  }

  """The default repository registry""" should
    """be able to register a repository""" in {
      val repo = AggregateRootRepository.dummy[TestPerson, TestPersonEvent]
      val registry = HasRepositories()
      registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
    }

  it should """be able to register a repository and retrieve it""" in {
    val repo = AggregateRootRepository.dummy[TestPerson, TestPersonEvent]
    val registry = HasRepositories()
    registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
    registry.getForAggregateRoot[TestPerson, TestPersonEvent].forceResult === repo
  }

  """The concurrent repository registry""" should
    """be able to register a repository""" in {
      val repo = AggregateRootRepository.dummy[TestPerson, TestPersonEvent]
      val registry = HasRepositories.concurrent
      registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
    }

  it should """be able to register a repository and retrieve it""" in {
    val repo = AggregateRootRepository.dummy[TestPerson, TestPersonEvent]
    val registry = HasRepositories.concurrent
    registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
    registry.getForAggregateRoot[TestPerson, TestPersonEvent].forceResult === repo
  }

}