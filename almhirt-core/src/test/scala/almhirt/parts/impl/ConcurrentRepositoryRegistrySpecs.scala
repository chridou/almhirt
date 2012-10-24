package almhirt.parts.impl

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.context.AlmhirtContextTestKit
import almhirt.eventsourcing.impl.DevNullEventLog

class ConcurrentRepositoryRegistrySpecs extends Specification with AlmhirtContextTestKit {
  """The concurrent repository registry""" should {
    """be able to register a repository""" in {
      inFakeContext(ctx => {
        val repo = new PersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new ConcurrentRepositoryRegistry()
        registry.register(repo)
        true
      })
    }
    """be able to register a repository and retrieve it""" in {
      inFakeContext(ctx => {
        val repo = new PersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new ConcurrentRepositoryRegistry()
        registry.register(repo)
        registry.get[PersonRepository].forceResult === repo
      })
    }
  }
}
