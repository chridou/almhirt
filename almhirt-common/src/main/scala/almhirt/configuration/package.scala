package almhirt

import almhirt.common._
import com.typesafe.config.Config

package object configuration {
  implicit class ConfigOps(self: Config) {
    def value[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      configExtractor.getValue(self, path)

    def optValue[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      configExtractor.tryGetValue(self, path)

    def unsafeValue[T](path: String)(implicit configExtractor: ConfigExtractor[T]): Option[T] =
      configExtractor.tryGetValue(self, path).fold(
        fail ⇒ None,
        succ ⇒ succ)

    /**
     * The value is a None, if and only if it is the case insensitive String "None".
     *  Otherwise it is parsed with the given Extractor.
     */
    def magicOption[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      ConfigStringExtractorInst.getValue(self, path).fold(
        problem => {
          self.value[T](path).map(Some(_))
        },
        mayBeMagicValue => {
          mayBeMagicValue.toLowerCase() match {
            case "none" => scalaz.Success(None)
            case _ => self.value[T](path).map(Some(_))
          }
        })

    final def v[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      value[T](path)(configExtractor)

    final def opt[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      optValue[T](path)(configExtractor)

    final def unsafeOpt[T](path: String)(implicit configExtractor: ConfigExtractor[T]): Option[T] =
      unsafeValue[T](path)(configExtractor)

  }

  implicit val ConfigStringExtractorInst = new ConfigStringExtractor {}
  implicit val ConfigBooleanExtractorInst = new ConfigBooleanExtractor {}
  implicit val ConfigIntExtractorInst = new ConfigIntExtractor {}
  implicit val ConfigLongExtractorInst = new ConfigLongExtractor {}
  implicit val ConfigDoubleExtractorInst = new ConfigDoubleExtractor {}
  implicit val ConfigFiniteDurationMsExtractorInst = new ConfigFiniteDurationMsExtractor {}
  implicit val ConfigJodaDurationMsExtractorInst = new ConfigJodaDurationMsExtractor {}
  implicit val ConfigUuidExtractorInst = new ConfigUuidExtractor {}
  implicit val ConfigUriExtractorInst = new ConfigUriExtractor {}

  implicit val ConfigStringListExtractorInst = new ConfigStringListExtractor {}
  implicit val ConfigBooleanListExtractorInst = new ConfigBooleanListExtractor {}
  implicit val ConfigIntListExtractorInst = new ConfigIntListExtractor {}
  implicit val ConfigLongListExtractorInst = new ConfigLongListExtractor {}
  implicit val ConfigDoubleListExtractorInst = new ConfigDoubleListExtractor {}
  implicit val ConfigFiniteDurationListMsExtractorInst = new ConfigFiniteDurationListMsExtractor {}
  implicit val ConfigJodaDurationListMsExtractorInst = new ConfigJodaDurationListMsExtractor {}
  implicit val ConfigUuidListExtractorInst = new ConfigUuidListExtractor {}
  implicit val ConfigUriListExtractorInst = new ConfigUriListExtractor {}
  implicit val ConfigConfigListExtractorExtractorInst = new ConfigConfigListExtractor {}

  implicit val ConfigConfigExtractorInst = new ConfigConfigExtractor {}
  implicit val ConfigJavaPropertiesExtractorInst = new ConfigJavaPropertiesExtractor {}
}