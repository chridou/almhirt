package almhirt.common

import almhirt.almvalidation.kit._

final case class ResourceSection(section: String) extends Function1[String, ResourceGroup] {
  def apply(group: String) = ResourceGroup(section, group)
  def withGroup(group: String) = ResourceGroup(section, group)
}

final case class ResourceGroup(section: String, group: String) extends Function1[String, ResourceKey] {
  def apply(key: String) = ResourceKey(section, group, key)
  def withKey(key: String) = ResourceKey(section, group, key)
  def withKeyPrefix(prefix: String) = ResourceKeyPrefix(section, group, prefix)
}

final case class ResourceKey(section: String, group: String, key: String)

object ResourceKey {
  def apply(raw: String): AlmValidation[ResourceKey] =
    raw.split("\\.") match {
      case Array(section, group, key) ⇒ scalaz.Success(ResourceKey(section, group, key))
      case _                          ⇒ scalaz.Failure(ArgumentProblem(s""""$raw" is not a valid resource key."""))
    }

  def unsafe(raw: String): ResourceKey =
    ResourceKey(raw).resultOrEscalate
}

final case class ResourceKeyPrefix(section: String, group: String, prefix: String) extends Function1[String, ResourceKey] {
  def apply(finalPart: String) = ResourceKey(section, group, s"$prefix$finalPart")

  def append(part: String) = ResourceKeyPrefix(section, group, s"$prefix$part")

  def prepend(part: String) = ResourceKeyPrefix(section, group, s"$part$prefix")
}
