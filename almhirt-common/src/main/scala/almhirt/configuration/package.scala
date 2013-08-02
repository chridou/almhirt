package almhirt

import almhirt.common._
import com.typesafe.config.Config

package object configuration {
  implicit class ConfigOps(self: Config) {
    def value[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      configExtractor.getValue(self, path)
    def optValue[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      configExtractor.tryGetValue(self, path)

    final def v[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      value[T](path)(configExtractor)

    final def opt[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      optValue[T](path)(configExtractor)
  }

  implicit val ConfigStringExtractorInst = new ConfigStringExtractor {}
  implicit val ConfigBooleanExtractorInst = new ConfigBooleanExtractor {}
  implicit val ConfigIntExtractorInst = new ConfigIntExtractor {}
  implicit val ConfigConfigExtractorInst = new ConfigConfigExtractor {}
  implicit val ConfigJavaPropertiesExtractorInst = new ConfigJavaPropertiesExtractor {}
}