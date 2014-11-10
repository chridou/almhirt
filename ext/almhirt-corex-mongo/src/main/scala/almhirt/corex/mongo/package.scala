package almhirt.corex

import almhirt.common._
import scalaz.Validation.FlatMap._

package object mongo {
  import almhirt.configuration._
  import com.typesafe.config.Config

  implicit object ResolveConfigExtractor extends ConfigExtractor[MongoConnectionSettings] {
    def getValue(config: Config, path: String): AlmValidation[MongoConnectionSettings] =
      for {
        section ← config.v[Config](path)
        hosts ← section.v[List[String]]("hosts")
        numChannelsPerNode ← section.magicDefault[Int]("default", 10)("num-channels-per-node")
      } yield MongoConnectionSettings(hosts, numChannelsPerNode = numChannelsPerNode)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[MongoConnectionSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }
  }

}