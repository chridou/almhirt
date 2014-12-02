package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Tree
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ResourceLookup {
  def allowsLocaleFallback: Boolean
  def supportedLocales: Set[ULocale]

  def resourceNodeStrict(locale: ULocale): AlmValidation[ResourceNode]

  def localesTree: Tree[ULocale]

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

  final def resourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, ResourceValue)] =
    resourceNode(locale).flatMap(node ⇒ node(key).map((node.locale, _)))

  final def formatable(key: ResourceKey, locale: ULocale): AlmValidation[Formatable] =
    for {
      res ← textResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case fmt: IcuMessageFormat ⇒
          new IcuFormattable(fmt.formatInstance).success
        case RawStringValue(pattern) ⇒
          IcuMessageFormat(pattern, res._1).map(ctr ⇒ new IcuFormattable(ctr.formatInstance))
      }
    } yield fmt

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
  final def findResource(key: ResourceKey, locale: ULocale): Option[ResourceValue] = resource(key, locale).toOption
  final def findResourceWithLocale(key: ResourceKey, locale: ULocale): Option[(ULocale, ResourceValue)] = resourceWithLocale(key, locale).toOption

  final def textResourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, TextResourceValue)] =
    resourceWithLocale(key, locale).flatMap {
      case (loc, res: TextResourceValue) ⇒ (loc, res).success
      case _                             ⇒ ArgumentProblem(s"Key $key does not map to a text resource.").failure
    }

  final def textResource(key: ResourceKey, locale: ULocale): AlmValidation[TextResourceValue] = textResourceWithLocale(key, locale).map(_._2)
  final def findTextResource(key: ResourceKey, locale: ULocale): Option[TextResourceValue] = textResource(key, locale).toOption
  final def findTextResourceWithLocale(key: ResourceKey, locale: ULocale): Option[(ULocale, TextResourceValue)] = textResourceWithLocale(key, locale).toOption

}