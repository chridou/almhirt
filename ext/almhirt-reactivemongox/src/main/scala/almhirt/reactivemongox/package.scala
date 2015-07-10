package almhirt

import scala.language.implicitConversions
import almhirt.common._
import almhirt.configuration._
import reactivemongo.api.commands.WriteConcern

/**
 * @author douven
 */
package object reactivemongox {
  implicit def almWc2RmWc(self: WriteConcernAlm): WriteConcern = WriteConcernAlm.toReactiveMongoWriteConcern(self)

  implicit val WriteConcernAlmConfigExtractor = new ConfigExtractor[WriteConcernAlm] {
   def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[WriteConcernAlm] =
      config.v[com.typesafe.config.Config](path).flatMap { WriteConcernAlm.fromConfig }
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[WriteConcernAlm]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }
}