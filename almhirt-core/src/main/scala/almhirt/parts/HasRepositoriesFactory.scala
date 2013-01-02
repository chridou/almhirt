package almhirt.parts

import almhirt.common._
import almhirt.environment._

trait RepositoryRegistryFactory {
  def createRepositoryRegistry(almhirt: Almhirt): AlmValidation[HasRepositories]
}
