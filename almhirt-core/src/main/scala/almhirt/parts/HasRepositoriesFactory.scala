package almhirt.parts

import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait RepositoryRegistryFactory {
  def createRepositoryRegistry(almhirt: Almhirt): AlmValidation[HasRepositories]
}
