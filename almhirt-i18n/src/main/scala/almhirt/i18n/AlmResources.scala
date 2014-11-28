package almhirt.i18n

import almhirt.common._
import com.ibm.icu.util.ULocale

trait ResourceLookup {
  def resource(key: ResourceKey, locale: ULocale): AlmValidation[String]
  final def findResource(key: ResourceKey, locale: ULocale): Option[String] = resource(key, locale).toOption
}

trait AlmResources extends ResourceLookup {
  def withFallback(fallback: AlmResources): AlmResources

  def resourceNode(locale: ULocale): AlmValidation[ResourceNode]
  final def findResourceNode(locale: ULocale): Option[ResourceNode] = resourceNode(locale).toOption
}

object AlmResources {

}