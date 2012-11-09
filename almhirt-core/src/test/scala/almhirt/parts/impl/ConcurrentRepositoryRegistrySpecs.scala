package almhirt.parts.impl

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.environment.AlmhirtContextTestKit
import almhirt.eventlog.impl.DevNullEventLog
import test._

class ConcurrentRepositoryRegistrySpecs extends Specification with AlmhirtContextTestKit {
  implicit val duration = akka.util.Duration(1, "s")
  """The concurrent repository registry""" should {
    """be able to register a repository""" in {
      inTestContext(implicit ctx => {
        val repo = AggregateRootRepository.basic[TestPerson, TestPersonEvent](TestPerson, new DevNullEventLog()(ctx))
        val registry = new ConcurrentRepositoryRegistry(ctx)
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
        true
      })
    }
    """be able to register a repository and retrieve it""" in {
      inTestContext(implicit ctx => {
        val repo = AggregateRootRepository.basic[TestPerson, TestPersonEvent](TestPerson, new DevNullEventLog()(ctx))
        val registry = new ConcurrentRepositoryRegistry(ctx)
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent](repo)
        registry.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult === repo
      })
    }
  }
}
