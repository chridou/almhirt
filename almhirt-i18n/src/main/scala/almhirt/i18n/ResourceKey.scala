package almhirt.i18n

import almhirt.common._
import com.ibm.icu.util.ULocale

final case class ResourceSection(section: String) {
  def withGroup(group: String) = ResourceGroup(section, group)
}

final case class ResourceGroup(section: String, group: String) {
  def withKey(key: String) = ResourceKey(section, group, key)
}

final case class ResourceKey(section: String, group: String, key: String) {
  def lookup(locale: ULocale)(implicit resources: AlmResources): AlmValidation[String] = resources.resource(this, locale)
  def find(locale: ULocale)(implicit resources: AlmResources): Option[String] = resources.findResource(this, locale)
}