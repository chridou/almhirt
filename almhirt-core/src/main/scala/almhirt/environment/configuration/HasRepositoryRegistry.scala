package almhirt.environment.configuration

import almhirt.parts.HasRepositories

trait HasRepositoryRegistry {
  def repositoryRegistry: HasRepositories
}