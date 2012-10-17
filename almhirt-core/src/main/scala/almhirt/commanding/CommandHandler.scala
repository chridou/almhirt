package almhirt.commanding

import almhirt.domain.AggregateRootRepository

trait CommandHandler[T <: DomainCommand] {
  def execute(command: T, repository: AggregateRootRepository, unitOfWork: UnitOfWork): Unit
}