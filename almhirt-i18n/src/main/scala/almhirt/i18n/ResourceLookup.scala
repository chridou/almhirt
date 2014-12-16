package almhirt.i18n

import java.text.FieldPosition
import scalaz.syntax.validation._
import scalaz.Tree
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
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
   * @return when true, it will use the root locale in case no node could be found for a given locale.
   */
  def fallsBackToRoot: Boolean

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
  final def resourceNode[L](locale: L)(implicit magnet: LocaleMagnet[L]): AlmValidation[ResourceNode] = {
    val uLoc = magnet.toULocale(locale)
    resourceNodeStrict(uLoc).fold(
      fail ⇒ {
        if (allowsLocaleFallback)
          resourceNodeStrict(uLoc.getFallback).fold(
            fail ⇒ resourceNodeStrict(localesTree.rootLabel),
            succ ⇒ succ.success)
        else
          ResourceNotFoundProblem(s""""${uLoc.getBaseName}" is not a supported locale.""").failure
      },
      succ ⇒ succ.success)
  }

  /**
   * Get a [[Formatable]] possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried [[Formatable]]
   * @param locale the locale for the queried [[Formatable]]
   * @return the possibly found [[Formatable]]
   */
  final def formatable[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[Formatable] =
    for {
      res ← textResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case fmt: IcuResourceValue ⇒
          new IcuFormatable(fmt.formatInstance).success
        case raw: RawStringResourceValue ⇒
          raw.success
        case f: BasicValueResourceValue ⇒
          f.formatable.success
      }
    } yield fmt

  /**
   * Get a String possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried String
   * @param locale the locale for the queried String
   * @return the possibly found String
   */
  final def rawText[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[String] =
    for {
      res ← textResource(key, locale)
      str ← res match {
        case fmt: IcuResourceValue ⇒
          inTryCatch { fmt.raw }
        case r: RawStringResourceValue ⇒
          r.raw.success
        case _: BasicValueResourceValue ⇒
          ArgumentProblem(s"""Value at key "$key". does not have a direct String representation so there is no direct renderable.""").failure
      }
    } yield str

  /**
   * If the given locale is not supported, try to make it a compatible locale that is supported.
   *
   * @param locale the locale that should be supported
   * @return A success if the locale is supported or if it could be transformed into a supported locale.
   */
  final def asSupportedLocale[L](locale: L)(implicit magnet: LocaleMagnet[L]): AlmValidation[ULocale] = {
    val uLoc = magnet.toULocale(locale)
    if (supportedLocales.contains(uLoc)) {
      uLoc.success
    } else if (allowsLocaleFallback) {
      val fb = uLoc.getFallback
      if (supportedLocales.contains(fb)) {
        fb.success
      } else {
        ArgumentProblem(s"""The locale "${uLoc.getBaseName}" is not supported neither is its fallback "${fb.getBaseName}".""").failure
      }
    } else {
      ArgumentProblem(s"""The locale "${uLoc.getBaseName}" is not supported.""").failure
    }
  }

  final def resource[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[ResourceValue] = resourceWithLocale(key, locale).map(_._2)

  final def resourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, ResourceValue)] =
    resourceNode(locale).flatMap(node ⇒ node(key).map((node.locale, _)))

  final def textResource[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[TextResourceValue] = textResourceWithLocale(key, locale).map(_._2)

  final def textResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, TextResourceValue)] =
    resourceWithLocale(key, locale).flatMap {
      case (loc, res: TextResourceValue) ⇒ (loc, res).success
      case _                             ⇒ ArgumentProblem(s"Key $key does not map to a text resource.").failure
    }
}

object ResourceLookup {
  implicit class ResourceLookUpOps(val self: ResourceLookup) extends AnyVal {
    def findResource[L: LocaleMagnet](key: ResourceKey, locale: L): Option[ResourceValue] = self.resource(key, locale).toOption
    def findResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): Option[(ULocale, ResourceValue)] = self.resourceWithLocale(key, locale).toOption
    def findTextResource[L: LocaleMagnet](key: ResourceKey, locale: L): Option[TextResourceValue] = self.textResource(key, locale).toOption
    def findTextResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): Option[(ULocale, TextResourceValue)] = self.textResourceWithLocale(key, locale).toOption

    def forceFormatable[T, L: LocaleMagnet](key: ResourceKey, locale: L): Formatable =
      self.formatable(key, locale).resultOrEscalate

    def formatItemInto[T, L](what: T, locale: L, appendTo: StringBuffer)(implicit renderer: ItemFormat[T], magnet: LocaleMagnet[L]): AlmValidation[StringBuffer] = {
      val uLoc = magnet.toULocale(locale)
      renderer.appendTo(what, uLoc, appendTo)(self)
    }

    def formatItem[T: ItemFormat, L: LocaleMagnet](what: T, locale: L): AlmValidation[String] = {
      formatItemInto(what, locale, new StringBuffer).map(_.toString)
    }

    def forceFormatItemInto[T, L: LocaleMagnet](what: T, locale: L, appendTo: StringBuffer)(implicit renderer: ItemFormat[T]): StringBuffer =
      formatItemInto(what, locale, appendTo).resultOrEscalate

    def forceFormatItem[T, L: LocaleMagnet](what: T, locale: L)(implicit renderer: ItemFormat[T]): String =
      formatItem(what, locale).resultOrEscalate

    def formatIntoBuffer[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
      for {
        formatable ← self.formatable(key, locale)
        res ← formatable.formatIntoBuffer(appendTo, args: _*)
      } yield res

    def formatArgsIntoBuffer[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
      for {
        formatable ← self.formatable(key, locale)
        res ← formatable.formatArgsIntoBuffer(appendTo, args)
      } yield res

    def format[L: LocaleMagnet](key: ResourceKey, locale: L, args: (String, Any)*): AlmValidation[String] =
      for {
        formatable ← self.formatable(key, locale)
        res ← formatable.format(args: _*)
      } yield res

    def formatArgs[L: LocaleMagnet](key: ResourceKey, locale: L, args: Map[String, Any]): AlmValidation[String] =
      for {
        formatable ← self.formatable(key, locale)
        res ← formatable.formatArgs(args)
      } yield res

  }
}

