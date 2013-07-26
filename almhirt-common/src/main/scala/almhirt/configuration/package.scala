package almhirt

import almhirt.common._
import com.typesafe.config.Config

package object configuration {
  implicit class ConfigOps(self: Config) {
    def value[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      configExtractor.getValue(self, path)
  }

  implicit val ConfigStringExtractorInst = new ConfigStringExtractor{}
  implicit val ConfigIntExtractorInst = new ConfigIntExtractor{}
  implicit val ConfigConfigExtractorInst = new ConfigConfigExtractor{}
  implicit val ConfigJavaPropertiesExtractorInst = new ConfigJavaPropertiesExtractor{}
}