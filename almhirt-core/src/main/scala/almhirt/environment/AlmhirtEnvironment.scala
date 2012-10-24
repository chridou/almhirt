package almhirt.environment

import almhirt.parts.HasRepositories

trait AlmhirtEnvironment {
  def context: AlmhirtContext
  def repositories: HasRepositories
}