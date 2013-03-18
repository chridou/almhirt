package almhirt.ext.core.slick.eventlogs

import java.util.{UUID => JUUID}
import org.joda.time.DateTime

final case class TextEventLogRow(id: JUUID, timestamp: DateTime, eventtype: String, channel: String, payload: String) 
final case class BinaryEventLogRow(id: JUUID, timestamp: DateTime, eventtype: String, channel: String, payload: Array[Byte]) 

final case class TextDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, timestamp: DateTime, eventtype: String, channel: String, payload: String) 
final case class BinaryDomainEventLogRow(id: JUUID, aggId: JUUID, aggVersion: Long, timestamp: DateTime, eventtype: String, channel: String, payload: Array[Byte]) 

final case class BlobRow(id: JUUID, data: Array[Byte])