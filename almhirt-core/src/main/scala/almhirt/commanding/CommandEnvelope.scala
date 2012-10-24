package almhirt.commanding

case class CommandEnvelope(command: DomainCommand, ticket: Option[String])