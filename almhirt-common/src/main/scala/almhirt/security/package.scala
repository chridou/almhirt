package almhirt

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.configuration._
import com.typesafe.config.Config

package object security {
  implicit object JsseSslSettingsConfigExtractor extends ConfigExtractor[JsseSslSettings] {
    def getValue(config: Config, path: String): AlmValidation[JsseSslSettings] =
      for {
        cfg ← config.v[Config](path)
        keyStore ← cfg.v[String]("key-store")
        keyStorePassword ← cfg.magicOption[String]("key-store-password")
        trustStore ← cfg.magicOption[String]("trust-store")
        trustStorePassword ← cfg.magicOption[String]("trust-store-password")
      } yield JsseSslSettings(keyStore, keyStorePassword, trustStore, trustStorePassword)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[JsseSslSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

}