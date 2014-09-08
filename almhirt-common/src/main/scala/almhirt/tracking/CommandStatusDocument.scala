package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common.CommandId

final case class CommandStatusDocument(commandId: CommandId, timestamp: LocalDateTime, status: CommandStatus)
