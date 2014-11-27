package almhirt.i18n

import almhirt.common._
import com.ibm.icu.util.ULocale

trait AlmResources {
  def resource(key: ResourceKey, locale: ULocale): AlmValidation[String]
  
  final def findResource(key: ResourceKey, locale: ULocale): Option[String] = resource(key, locale).toOption
  
  def withFallback(fallback: AlmResources): AlmResources
}