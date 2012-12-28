package almhirt.parts.impl

import scala.concurrent.duration.Duration
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.environment.AlmhirtContextTestKit
import almhirt.eventlog._
import almhirt.parts.HasRepositories
import test._
import org.specs2.mutable._

class RepositoryRegistrySpecs extends Specification with AlmhirtContextTestKit {
  implicit val duration = Duration(1, "s")
  """The unsafe repository registry""" should {
    """be able to register a repository""" in {
      inTestContext( implicit ctx => {
        implicit val system = ctx.system 
        val repo = AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, DomainEventLog.devNull()(system).forceResult)
        val registry = HasRepositories().forceResult
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
        true
      })
    }
    """be able to register a repository and retrieve it""" in {
      inTestContext(implicit ctx => {
        implicit val system = ctx.system 
        val repo = AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, DomainEventLog.devNull()(system).forceResult)
        val registry = HasRepositories().forceResult
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
        registry.getForAggregateRoot[TestPerson, TestPersonEvent].forceResult === repo
      })
    }
  }
}