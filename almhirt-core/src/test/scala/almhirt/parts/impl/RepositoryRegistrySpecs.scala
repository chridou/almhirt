package almhirt.parts.impl

import org.scalatest._
import scala.concurrent.duration._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.environment.AlmhirtTestKit
import almhirt.eventlog._
import almhirt.parts.HasRepositories
import test._
import akka.testkit.TestEvent

class RepositoryRegistrySpecs extends FlatSpec with BeforeAndAfterAll with AlmhirtTestKit {
  private[this] val (theAlmhirt, shutDown) = createTestAlmhirt()
  implicit val atMost = FiniteDuration(1, "s")
  implicit val alm = theAlmhirt
  implicit val executionContext = theAlmhirt.executionContext
 
  override def afterAll {
    shutDown.shutDown
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