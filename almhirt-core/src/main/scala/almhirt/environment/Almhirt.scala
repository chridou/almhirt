package almhirt.environment

import akka.util.Duration
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import almhirt.messaging._
import org.joda.time.DateTime
import akka.dispatch.MessageDispatcher

trait Almhirt extends AlmhirtBaseOps with CreatesMessageChannels with ServiceRegistry with CanCreateUuidsAndDateTimes with Closeable{
  def executeCommand(cmd: DomainCommand, ticket: Option[TrackingTicket]) { executeCommand(CommandEnvelope(cmd, ticket)) }
  def executeTrackedCommand(cmd: DomainCommand, ticket: TrackingTicket) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
}