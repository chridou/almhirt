package almhirt

import scala.language.implicitConversions
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.configuration._
import reactivemongo.api.commands.WriteConcern
import reactivemongo.api.ReadPreference

package object reactivemongox {
  implicit def almWc2RmWc(self: WriteConcernAlm): WriteConcern = WriteConcernAlm.toReactiveMongoWriteConcern(self)
  implicit def almRp2RmRp(self: ReadPreferenceAlm): ReadPreference = ReadPreferenceAlm.toReactiveMongoReadPreference(self)

  implicit val WriteConcernAlmConfigExtractorInst = new ConfigExtractor[WriteConcernAlm] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[WriteConcernAlm] =
      config.v[com.typesafe.config.Config](path).flatMap { WriteConcernAlm.fromConfig }
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[WriteConcernAlm]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadPreferenceAlmConfigExtractorInst = new ConfigExtractor[ReadPreferenceAlm] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadPreferenceAlm] =
      config.v[com.typesafe.config.Config](path).flatMap { ReadPreferenceAlm.fromConfig }
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadPreferenceAlm]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeSupportsReadingConfigExtractorInst = new ConfigExtractor[ReadWriteMode.SupportsReading] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode.SupportsReading] =
      for {
        rp ← config.v[ReadPreferenceAlm]("read-preference")
        wc ← config.magicOption[WriteConcernAlm]("write-concern")
      } yield wc match {
        case Some(wc) ⇒ ReadWriteMode.ReadAndWrite(rp, wc)
        case None     ⇒ ReadWriteMode.ReadOnly(rp)
      }
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode.SupportsReading]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeSupportsWritingConfigExtractorInst = new ConfigExtractor[ReadWriteMode.SupportsWriting] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode.SupportsWriting] =
      for {
        rp ← config.magicOption[ReadPreferenceAlm]("read-preference")
        wc ← config.v[WriteConcernAlm]("write-concern")
      } yield rp match {
        case Some(rp) ⇒ ReadWriteMode.ReadAndWrite(rp, wc)
        case None     ⇒ ReadWriteMode.WriteOnly(wc)
      }
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode.SupportsWriting]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeConfigExtractorInst = new ConfigExtractor[ReadWriteMode] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode] =
      for {
        rp ← config.magicOption[ReadPreferenceAlm]("read-preference")
        wc ← config.magicOption[WriteConcernAlm]("write-concern")
      } yield ReadWriteMode(rp, wc)
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }
}