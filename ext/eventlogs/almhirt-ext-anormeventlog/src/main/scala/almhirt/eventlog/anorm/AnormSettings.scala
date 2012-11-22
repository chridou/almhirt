package almhirt.eventlog.anorm

import java.util.Properties

case class AnormSettings(connection: String, props: Properties, logTableName: String)
