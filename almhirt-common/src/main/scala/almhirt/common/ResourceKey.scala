package almhirt.common

import almhirt.almvalidation.kit._

/**
 * The outermost(1st level) identifying level for resources
 */
final case class ResourceSection(section: String) extends Function1[String, ResourceGroup] {
  /**
   * Create a [[almhirt.common.ResourceGroup]].
   * It is assumed that the group parameter does not contain any dots.
   * 
   * Same as #withGroup
   *
   * @param group the name of the group
   * @return the [[almhirt.common.ResourceGroup]]
   */
  def apply(group: String) = ResourceGroup(section, group)

  /**
   * Create a [[almhirt.common.ResourceGroup]].
   * It is assumed that the group parameter does not contain any dots.
   *
   * @param group the name of the group
   * @return the [[almhirt.common.ResourceGroup]]
   */
  def withGroup(group: String) = ResourceGroup(section, group)

  /**
   * Create a [[almhirt.common.ResourceKey]] by using a dot as a group/key separator
   * This function may fail.
   *
   * @param groupAndKey group- and key name separated by a dot
   * @return the [[almhirt.common.ResourceKey]]
   */
  def makeKey(groupAndKey: String): AlmValidation[ResourceKey] =
    groupAndKey.split("\\.") match {
      case Array(group, key) ⇒ scalaz.Success(ResourceKey(section, group, key))
      case _                 ⇒ scalaz.Failure(ArgumentProblem(s""""$groupAndKey" is not suitable to complete section "$section" to a key."""))
    }

  /**
   * Create a [[almhirt.common.ResourceKey]] by using the first dot as a group/key separator.
   * All dots after the first will be replaced by a given string
   * This function may fail.
   *
   * @param groupAndKey group- and key name separated by a dot
   * @param superflousDotReplacement the string that replaces all contained dots after the first
   * @return the [[almhirt.common.ResourceKey]]
   */
  def makeKey(groupAndKey: String, superflousDotReplacement: String): AlmValidation[ResourceKey] =
    groupAndKey.split("\\.") match {
      case Array(group, key)       ⇒ scalaz.Success(ResourceKey(section, group, key))
      case Array(group, rest @ _*) ⇒ scalaz.Success(ResourceKey(section, group, rest.mkString(superflousDotReplacement)))
      case _                       ⇒ scalaz.Failure(ArgumentProblem(s""""$groupAndKey" is not suitable to complete section "$section" to a key."""))
    }

  /**
   * Create a [[almhirt.common.ResourceKey]] by using a dot as a group/key separator
   * This function may throw an exception.
   *
   * @param groupAndKey group- and key name separated by a dot
   * @return the [[almhirt.common.ResourceKey]]
   */
  def forceKey(groupAndKey: String): ResourceKey =
    makeKey(groupAndKey).resultOrEscalate

  /**
   * Create a [[almhirt.common.ResourceKey]] by using the first dot as a group/key separator.
   * All dots after the first will be replaced by a given string
   * This function may throw an exception.
   *
   * @param groupAndKey group- and key name separated by a dot
   * @param superflousDotReplacement the string that replaces all contained dots after the first
   * @return the [[almhirt.common.ResourceKey]]
   */
  def forceKey(groupAndKey: String, superflousDotReplacement: String): ResourceKey =
    makeKey(groupAndKey, superflousDotReplacement).resultOrEscalate
}

/**
 * The outermost second identifying level for resources
 */
final case class ResourceGroup(section: String, group: String) extends Function1[String, ResourceKey] {
  /**
   * Create a [[almhirt.common.ResourceKey]]
   * It is assumed that the key parameter does not contain any dots.
   * 
   * Same as #withKey
   *
   * @param group the name of the key
   * @return the [[almhirt.common.ResourceKey]]
   */
  def apply(key: String) = ResourceKey(section, group, key)

  /**
   * Create a [[almhirt.common.ResourceKey]]
   * It is assumed that the key parameter does not contain any dots.
   *
   * @param group the name of the key
   * @return the [[almhirt.common.ResourceKey]]
   */
  def withKey(key: String) = ResourceKey(section, group, key)

  /**
   * Create a [[almhirt.common.ResourceKeyPrefix]]
   * It is assumed that the prefix parameter does not contain any dots.
   *
   * @param prefix the prefix of the key name
   * @return the [[almhirt.common.ResourceKey]]
   */
  def withKeyPrefix(prefix: String) = ResourceKeyPrefix(section, group, prefix)

}

/**
 * The innermost(3rd level) identifying level for resources
 */
final case class ResourceKey(section: String, group: String, key: String)

object ResourceKey {
  /**
   * Parse a string that must contain 2 dots into a [[almhirt.common.ResourceKey]]
   * This function may fail.
   * 
   * Same as parse 
   *
   * @param toParse a string to parse
   * @return the [[almhirt.common.ResourceKey]] made from the string
   */
  def apply(toParse: String): AlmValidation[ResourceKey] =
    toParse.split("\\.") match {
      case Array(section, group, key) ⇒ scalaz.Success(ResourceKey(section, group, key))
      case _                          ⇒ scalaz.Failure(ArgumentProblem(s""""$toParse" is not a valid resource key."""))
    }

  /**
   * Parse a string that must contain 2 dots into a [[almhirt.common.ResourceKey]]
   * This function may fail.
   *
   * @param toParse a string to parse
   * @return the [[almhirt.common.ResourceKey]] made from the string
   */
  def parse(toParse: String): AlmValidation[ResourceKey] =
    toParse.split("\\.") match {
      case Array(section, group, key) ⇒ scalaz.Success(ResourceKey(section, group, key))
      case _                          ⇒ scalaz.Failure(ArgumentProblem(s""""$toParse" is not a valid resource key."""))
    }

  /**
   * Parse a string that must contain 2 dots into a [[almhirt.common.ResourceKey]]
   * This function may throw an exception.
   *
   * @param toParse a string to parse
   * @return the [[almhirt.common.ResourceKey]] made from the string
   */
  def forceParse(toParse: String): ResourceKey =
    ResourceKey(toParse).resultOrEscalate
}

/**
 * An unfinished key key. Its name is not yet complete.
 * Parts of the complete key name can be appended (or even prepended) to the contained prefix.
 */
final case class ResourceKeyPrefix(section: String, group: String, prefix: String) extends Function1[String, ResourceKey] {
  /**
   * Append finalPart to the prefix.
   * It is assumed that the finalPart parameter does not contain any dots.
   * 
   * Same as append.
   *
   * @param finalPart the final part that makes the key complete
   * @return the created key
   */
  def apply(finalPart: String) = ResourceKey(section, group, s"$prefix$finalPart")

  /**
   * Append a part to the prefix.
   * It is assumed that the toAppend parameter does not contain any dots.
   *
   * @param toAppend a string to append to the current prefix
   * @return a new [[almhirt.common.ResourceKeyPrefix]]
   */
  def append(toAppend: String) = ResourceKeyPrefix(section, group, s"$prefix$toAppend")

  /**
   * Prepend a part to the prefix.
   * It is assumed that the finalPart parameter does not contain any dots.
   *
   * @param toPrepend a string to prepend to the current prefix
   * @return a new [[almhirt.common.ResourceKeyPrefix]]
   */
  def prepend(toPrepend: String) = ResourceKeyPrefix(section, group, s"$toPrepend$prefix")

  /**
   * When the contained prefix is already a complete key, call this method.
   *
   * @return the [[almhirt.common.ResourceKey]] made from the contents of this [[almhirt.common.ResourceKeyPrefix]]
   */
  def key = ResourceKey(section, group, prefix)
}
