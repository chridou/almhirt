package almhirt.eventlog.impl.jdbc

import java.util.UUID
import java.util.Properties
import org.joda.time.DateTime

case class JdbcEventLogSettings(connection: String, logTableName: Option[String], props: Properties, drivername: String)

case class JdbcEventLogEntry(id: UUID, version: Long, timestamp: DateTime, payload: String)
