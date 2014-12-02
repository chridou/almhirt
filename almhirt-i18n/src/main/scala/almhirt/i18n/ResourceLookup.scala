package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Tree
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

/**
 * Looks up resources by locale and provides methods for locale transformation and supported locales.
 * This one is thread safe.
 */
trait ResourceLookup {
  /**
   * @return when true, it will use fallback locales to find a resource.
   */
  def allowsLocaleFallback: Boolean

  /**
   * @return the set of supported locales that are supported without using fallbacks
   */
  def supportedLocales: Set[ULocale]

  /**
   * @return a tree of that represents the structure of the [[ResourceNode]]s
   */
  def localesTree: Tree[ULocale]

  /**
   * Get a [[ResourceNode]] without using a fallback locale
   *
   * @param locale the exact locale for the queried [[ResourceNode]]
   * @return the possibly found [[ResourceNode]]
   */
  def resourceNodeStrict(locale: ULocale): AlmValidation[ResourceNode]

  /**
   * Get a [[ResourceNode]] possibly using a fallback locale
   *
   * @param locale the locale for the queried [[ResourceNode]]
   * @return the possibly found [[ResourceNode]]
   */
  final def resourceNode(locale: ULocale): AlmValidation[ResourceNode] = {
    resourceNodeStrict(locale).fold(
      fail ⇒ {
        if (allowsLocaleFallback)
          resourceNodeStrict(locale.getFallback)
        else
          ResourceNotFoundProblem(s""""${locale.getBaseName}" is not a supported locale.""").failure
      },
      succ ⇒ succ.success)
  }

  /**
   * Get a [[Formatable]] possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried [[Formatable]]
   * @param locale the locale for the queried [[Formatable]]
   * @return the possibly found [[ResourceNode]]
   */
  final def formatable(key: ResourceKey, locale: ULocale): AlmValidation[Formatable] =
    for {
      res ← textResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case fmt: IcuMessageFormat ⇒
          new IcuFormattable(fmt.formatInstance).success
        case RawStringValue(pattern) ⇒
          IcuMessageFormat(pattern, res._1).map(messageformat ⇒ new IcuFormattable(messageformat.formatInstance))
      }
    } yield fmt

  /**
   * If the given locale is not supported, try to make it a compatible locale that is supported.
   * 
   * @param locale the locale that should be supported
   * @return A success if the locale is supported or if it could be transformed into a supported locale.
   */
  final def asSupportedLocale(locale: ULocale): AlmValidation[ULocale] =
    if (supportedLocales.contains(locale)) {
      locale.success
    } else if (allowsLocaleFallback) {
      val fb = locale.getFallback
      if (supportedLocales.contains(fb)) {
        fb.success
      } else {
        ArgumentProblem(s"""The locale "${locale.getBaseName}" is not supported neither is its fallback "${fb.getBaseName}".""").failure
      }
    } else {
      ArgumentProblem(s"""The locale "${locale.getBaseName}" is not supported.""").failure
    }

  final def resource(key: ResourceKey, locale: ULocale): AlmValidation[ResourceValue] = resourceWithLocale(key, locale).map(_._2)
  final def resourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, ResourceValue)] =
    resourceNode(locale).flatMap(node ⇒ node(key).map((node.locale, _)))
  final def textResource(key: ResourceKey, locale: ULocale): AlmValidation[TextResourceValue] = textResourceWithLocale(key, locale).map(_._2)
  final def textResourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, TextResourceValue)] =
    resourceWithLocale(key, locale).flatMap {
      case (loc, res: TextResourceValue) ⇒ (loc, res).success
      case _                             ⇒ ArgumentProblem(s"Key $key does not map to a text resource.").failure
    }
}

object ResourceLookup {
  implicit class ResourceLookUpOps(val self: ResourceLookup) extends AnyVal {
    def findResource(key: ResourceKey, locale: ULocale): Option[ResourceValue] = self.resource(key, locale).toOption
    def findResourceWithLocale(key: ResourceKey, locale: ULocale): Option[(ULocale, ResourceValue)] = self.resourceWithLocale(key, locale).toOption
    def findTextResource(key: ResourceKey, locale: ULocale): Option[TextResourceValue] = self.textResource(key, locale).toOption
    def findTextResourceWithLocale(key: ResourceKey, locale: ULocale): Option[(ULocale, TextResourceValue)] = self.textResourceWithLocale(key, locale).toOption

    def renderItemInto[T](what: T, locale: ULocale, buffer: StringBuffer)(implicit itemFormatter: ItemFormatter[T]): AlmValidation[StringBuffer] =
      for {
        prepared ← itemFormatter.prepare(what, locale, self)
        rendered ← prepared.renderIntoBuffer(buffer)
      } yield rendered

    def renderItem[T](what: T, locale: ULocale)(implicit itemFormatter: ItemFormatter[T]): AlmValidation[String] =
      renderItemInto(what, locale, new StringBuffer).map(_.toString())
  }
}

