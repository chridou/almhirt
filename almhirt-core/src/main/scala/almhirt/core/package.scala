package almhirt

import almhirt.common._
import almhirt.messaging.MessageChannel
import almhirt.domain.DomainEvent

package object core {
  type EventChannel = MessageChannel[Event]
  type DomainEventChannel = MessageChannel[DomainEvent]
  type CommandChannel = MessageChannel[Command]
  
  implicit val ConfigDurationsExtractorInst = new almhirt.configuration.ConfigExtractor[Durations] {
    import almhirt.almvalidation.kit._
    import almhirt.configuration._
    import com.typesafe.config.Config
    
  def getValue(config: Config, path: String): AlmValidation[Durations] =
    config.v[Config](path).flatMap(sect => Durations(sect))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Durations]] =
    config.opt[Config](path).flatMap(sectOpt => sectOpt.map(sect => Durations(sect)).validationOut)
    
  }

  implicit val ConfigHasDurationsExtractorInst = new almhirt.configuration.ConfigExtractor[HasDurations] {
    import almhirt.almvalidation.kit._
    import almhirt.configuration._
    import com.typesafe.config.Config
    
  def getValue(config: Config, path: String): AlmValidation[HasDurations] =
    config.v[Config](path).flatMap(sect => HasDurations(sect))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[HasDurations]] =
    config.opt[Config](path).flatMap(sectOpt => sectOpt.map(sect => HasDurations(sect)).validationOut)
  }
  
}