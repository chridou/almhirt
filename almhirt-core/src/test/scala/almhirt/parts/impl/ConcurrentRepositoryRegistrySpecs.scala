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
      inFakeContext(implicit ctx => {
        val repo = new TestPersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new ConcurrentRepositoryRegistry(ctx)
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent, TestPersonRepository](repo)
        true
      })
    }
    """be able to register a repository and retrieve it""" in {
      inFakeContext(ctx => {
        val repo = new TestPersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new ConcurrentRepositoryRegistry(ctx)
        registry.registerForAggregateRoot[TestPerson, TestPersonEvent, TestPersonRepository](repo)
        registry.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult === repo
      })
    }
  }
}
