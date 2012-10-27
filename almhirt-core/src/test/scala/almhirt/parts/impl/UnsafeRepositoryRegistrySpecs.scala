package almhirt.parts.impl

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.environment.AlmhirtContextTestKit
import almhirt.eventlog.impl.DevNullEventLog
import test._

class UnsafeRepositoryRegistrySpecs extends Specification with AlmhirtContextTestKit {
  """The unsafe repository registry""" should {
    """be able to register a repository""" in {
      inFakeContext(ctx => {
        val repo = new TestPersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new UnsafeRepositoryRegistry()
        registry.register(repo)
        true
      })
    }
    """be able to register a repository and retrieve it""" in {
      inFakeContext(ctx => {
        val repo = new TestPersonRepository(new DevNullEventLog()(ctx))(ctx)
        val registry = new UnsafeRepositoryRegistry()
        registry.register(repo)
        registry.get[TestPersonRepository].forceResult === repo
      })
    }
  }
}