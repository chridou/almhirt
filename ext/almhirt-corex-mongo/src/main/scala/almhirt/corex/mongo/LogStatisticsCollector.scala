package almhirt.corex.mongo

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.domaineventlog._

object LogStatisticsCollector {
  sealed trait LogStatisticsCollectorMessage
  case class AddWriteDuration(dur: FiniteDuration)
  case class AddReadDuration(dur: FiniteDuration)
  case class AddSerializationDuration(dur: FiniteDuration)
  case class AddDeserializationDuration(dur: FiniteDuration)
}

class LogStatisticsCollector extends Actor with ActorLogging {
  import LogStatisticsCollector._
  protected var writeStatistics = DomainEventLogWriteStatistics()
  protected var readStatistics = DomainEventLogReadStatistics()
  protected var serializationStatistics = DomainEventLogSerializationStatistics.forSerializing
  protected var deserializationStatistics = DomainEventLogSerializationStatistics.forDeserializing
  
  override def receive: Receive = {
    case AddWriteDuration(dur) =>  writeStatistics = writeStatistics add dur 
    case AddReadDuration(dur) =>  readStatistics = readStatistics add dur 
    case AddSerializationDuration(dur) =>  serializationStatistics = serializationStatistics add dur 
    case AddDeserializationDuration(dur) =>  deserializationStatistics = deserializationStatistics add dur 
  }
  
  override def postStop() {
    log.info(s"""\n |Final statistics 
    				|${writeStatistics.toNiceString}
    				|${readStatistics.toNiceString}
    				|${serializationStatistics.toNiceString}
    				|${deserializationStatistics.toNiceString}""".stripMargin)
  }
}