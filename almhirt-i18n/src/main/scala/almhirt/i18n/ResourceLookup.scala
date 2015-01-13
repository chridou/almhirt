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
   * @return when true, it will move upwards in the locale hierarchy to lookup a key in case it is not found for the current locale.
   */
  def doesUpwardLookup: Boolean

  /**
   * @return the set of supported locales that are supported without using fallbacks
   */
  def supportedLocales: Set[ULocale]

  /**
   * @return a tree of that represents the structure of the locales
   */
  def localeTree: Tree[ULocale]

  /**
   * Get an [[AlmFormatter]] possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried [[AlmFormatter]]
   * @param locale the locale for the queried [[AlmFormatter]]
   * @return the possibly found [[AlmFormatter]]
   */
  final def getFormatter[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[AlmFormatter] =
    for {
      res ← getTextResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case fmt: IcuResourceValue ⇒
          new IcuFormatter(fmt.formatInstance).success
        case raw: RawStringResourceValue ⇒
          raw.success
        case f: BasicValueResourceValue ⇒
          f.formatable.success
      }
    } yield fmt

  /**
   * Get an [[AlmNumericFormatter]] possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried [[AlmNumericFormatter]]
   * @param locale the locale for the queried [[AlmNumericFormatter]]
   * @return the possibly found [[AlmNumericFormatter]]
   */
  final def getNumericFormatter[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[AlmNumericFormatter] =
    for {
      res ← getResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case f: NumericValueResourceValue ⇒
          f.formatable.success
        case x ⇒
          ArgumentProblem(s"""Value at key "$key" is not a numeric formatter.""").failure

      }
    } yield fmt

  /**
   * Get an [[AlmMeasureFormatter]] possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried [[AlmMeasureFormatter]]
   * @param locale the locale for the queried [[AlmMeasureFormatter]]
   * @return the possibly found [[AlmMeasureFormatter]]
   */
  final def getMeasureFormatter[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[AlmMeasureFormatter] =
    for {
      res ← getResourceWithLocale(key, locale)
      fmt ← res._2 match {
        case f: MeasuredValueResourceValue ⇒
          f.formatable.success
        case x ⇒
          ArgumentProblem(s"""Value at key "$key" is not a measure formatter.""").failure
      }
    } yield fmt

  /**
   * Get a String possibly using a fallback locale
   *
   * @param key the [[ResourceKey]] for the queried String
   * @param locale the locale for the queried String
   * @return the possibly found String
   */
  final def getRawText[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[String] =
    for {
      res ← getTextResource(key, locale)
      str ← res match {
        case fmt: IcuResourceValue ⇒
          inTryCatch { fmt.raw }
        case r: RawStringResourceValue ⇒
          r.raw.success
        case _: BasicValueResourceValue ⇒
          ArgumentProblem(s"""Value at key "$key" does not have a direct String representation so there is no direct renderable.""").failure
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
      } else if (fallsBackToRoot) {
        localeTree.rootLabel.success
      } else {
        ArgumentProblem(s"""The locale "${uLoc.getBaseName}" is not supported neither is its fallback "${fb.getBaseName}".""").failure
      }
    } else if (fallsBackToRoot) {
      localeTree.rootLabel.success
    } else {
      ArgumentProblem(s"""The locale "${uLoc.getBaseName}" is not supported.""").failure
    }
  }

  def getResource[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[ResourceValue] = getResourceWithLocale(key, locale).map(_._2)

  def getResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, ResourceValue)]

  final def getTextResource[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[TextResourceValue] = getTextResourceWithLocale(key, locale).map(_._2)

  final def getTextResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, TextResourceValue)] =
    getResourceWithLocale(key, locale).flatMap {
      case (loc, res: TextResourceValue) ⇒ (loc, res).success
      case _                             ⇒ ArgumentProblem(s"Key $key does not map to a text resource.").failure
    }
}

object ResourceLookup {
  implicit class ResourceLookupBasicOps(val self: ResourceLookup) extends AnyVal {
    def findResource[L: LocaleMagnet](key: ResourceKey, locale: L): Option[ResourceValue] = self.getResource(key, locale).toOption
    def findResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): Option[(ULocale, ResourceValue)] = self.getResourceWithLocale(key, locale).toOption
    def findTextResource[L: LocaleMagnet](key: ResourceKey, locale: L): Option[TextResourceValue] = self.getTextResource(key, locale).toOption
    def findTextResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): Option[(ULocale, TextResourceValue)] = self.getTextResourceWithLocale(key, locale).toOption

    def forceFormatter[T, L: LocaleMagnet](key: ResourceKey, locale: L): AlmFormatter =
      self.getFormatter(key, locale).resultOrEscalate

    def rawText[L: LocaleMagnet](key: ResourceKey, locale: L): String =
      self.getRawText(key, locale) fold (
        fail ⇒ s"{$key: ${fail.message}}",
        succ ⇒ succ)
  }

  implicit class ResourceLookupItemOps(val self: ResourceLookup) extends AnyVal {
    def formatItemInto[T, L](what: T, locale: L, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(implicit itemFormatter: ItemFormatter[T], magnet: LocaleMagnet[L]): AlmValidation[StringBuffer] = {
      val uLoc = magnet.toULocale(locale)
      itemFormatter.appendToBuffer(what, uLoc, uomSys, appendTo)(self)
    }

    def formatItemInto[T, L](what: T, locale: L, appendTo: StringBuffer)(implicit itemFormatter: ItemFormatter[T], magnet: LocaleMagnet[L]): AlmValidation[StringBuffer] = {
      val uLoc = magnet.toULocale(locale)
      itemFormatter.appendToBuffer(what, uLoc, appendTo)(self)
    }

    def formatItem[T: ItemFormatter, L: LocaleMagnet](what: T, locale: L, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] = {
      formatItemInto(what, locale, uomSys, new StringBuffer).map(_.toString)
    }

    def formatItem[T: ItemFormatter, L: LocaleMagnet](what: T, locale: L): AlmValidation[String] = {
      formatItemInto(what, locale, new StringBuffer).map(_.toString)
    }

    def forceFormatItemInto[T, L: LocaleMagnet](what: T, locale: L, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(implicit renderer: ItemFormatter[T]): StringBuffer =
      formatItemInto(what, locale, uomSys, appendTo) fold (
        fail ⇒ appendTo.append(s"{${what.getClass().getSimpleName}: ${fail.message}}"),
        succ ⇒ succ)

    def forceFormatItemInto[T, L: LocaleMagnet](what: T, locale: L, appendTo: StringBuffer)(implicit renderer: ItemFormatter[T]): StringBuffer =
      formatItemInto(what, locale, appendTo) fold (
        fail ⇒ appendTo.append(s"{${what.getClass().getSimpleName}: ${fail.message}}"),
        succ ⇒ succ)

    def forceFormatItem[T, L: LocaleMagnet](what: T, locale: L, uomSys: Option[UnitsOfMeasurementSystem])(implicit renderer: ItemFormatter[T]): String =
      formatItem(what, locale, uomSys) fold (
        fail ⇒ s"{${what.getClass().getSimpleName}: ${fail.message}}",
        succ ⇒ succ)

    def forceFormatItem[T, L: LocaleMagnet](what: T, locale: L)(implicit renderer: ItemFormatter[T]): String =
      formatItem(what, locale) fold (
        fail ⇒ s"{${what.getClass().getSimpleName}: ${fail.message}}",
        succ ⇒ succ)
  }

  object SafeFormattingImplicits {
    implicit class ResourceLookupSafeFormatterOps(val self: ResourceLookup) extends AnyVal {
      def formatIntoBuffer[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.formatInto(appendTo, args: _*)
        } yield res

      def formatArgsIntoBuffer[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.formatArgsInto(appendTo, args)
        } yield res

      def format[L: LocaleMagnet](key: ResourceKey, locale: L, args: (String, Any)*): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.format(args: _*)
        } yield res

      def formatArgs[L: LocaleMagnet](key: ResourceKey, locale: L, args: Map[String, Any]): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.formatArgs(args)
        } yield res

      def formatValuesInto[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.formatValuesInto(appendTo, values: _*)
        } yield res

      def formatValues[L: LocaleMagnet](key: ResourceKey, locale: L, values: Any*): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key, locale)
          res ← formatable.formatValues(values: _*)
        } yield res

      def formatNumericIntoAt[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, num: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getNumericFormatter(key, locale)
          res ← formatable.formatNumericIntoAt(num, appendTo, pos)
        } yield res

      def formatNumericInto[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, num: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
        formatNumericIntoAt(key, locale, num, appendTo, util.DontCareFieldPosition)

      def formatNumeric[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, num: T): AlmValidation[String] =
        formatNumericIntoAt(key, locale, num, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())

      def formatNumericRangeIntoAt[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getNumericFormatter(key, locale)
          res ← formatable.formatNumericRangeIntoAt(upper, lower, appendTo, pos)
        } yield res

      def formatNumericRangeInto[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, lower: T, upper: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
        formatNumericRangeIntoAt(key, locale, lower, upper, appendTo, util.DontCareFieldPosition)

      def formatNumericRange[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, lower: T, upper: T): AlmValidation[String] =
        formatNumericRangeIntoAt(key, locale, lower, upper, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())

      def formatMeasureIntoAt[L: LocaleMagnet](key: ResourceKey, locale: L, v: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getMeasureFormatter(key, locale)
          res ← formatable.formatMeasureIntoAt(v, appendTo, pos, uomSys)
        } yield res

      def formatMeasureInto[L: LocaleMagnet](key: ResourceKey, locale: L, v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        formatMeasureIntoAt(key, locale, v, appendTo, util.DontCareFieldPosition, uomSys)

      def formatMeasure[L: LocaleMagnet](key: ResourceKey, locale: L, v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
        formatMeasureIntoAt(key, locale, v, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())

      def formatMeasureRangeIntoAt[L: LocaleMagnet](key: ResourceKey, locale: L, lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getMeasureFormatter(key, locale)
          res ← formatable.formatMeasureRangeIntoAt(lower, upper, appendTo, pos, uomSys)
        } yield res

      def formatMeasureRangeInto[L: LocaleMagnet](key: ResourceKey, locale: L, lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        formatMeasureRangeIntoAt(key, locale, lower, upper, appendTo, util.DontCareFieldPosition, uomSys)

      def formatMeasureRange[L: LocaleMagnet](key: ResourceKey, locale: L, lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
        formatMeasureRangeIntoAt(key, locale, lower, upper, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())
    }
  }

  object UnsafeFormattingImplicits {
    private def intoBuffer[T](key: ResourceKey, buffer: StringBuffer, f1: () ⇒ AlmValidation[T], f2: (T, StringBuffer) ⇒ AlmValidation[StringBuffer]): StringBuffer =
      f1() fold (
        fail ⇒ buffer.append(s"{$key}"),
        succ1 ⇒ f2(succ1, buffer) fold (
          fail ⇒ buffer.append(s"{$key: ${fail.message}}"),
          succ2 ⇒ succ2))

    implicit class ResourceLookupUnsafeFormatterOps(val self: ResourceLookup) extends AnyVal {
      def forceFormatInto[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: (String, Any)*): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatInto(buffer, args: _*))

      def forceFormatArgsInto[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, args: Map[String, Any]): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatArgsInto(buffer, args))

      def forceFormat[L: LocaleMagnet](key: ResourceKey, locale: L, args: (String, Any)*): String =
        forceFormatInto(key, locale, new StringBuffer, args: _*).toString()

      def forceFormatArgs[L: LocaleMagnet](key: ResourceKey, locale: L, args: Map[String, Any]): String =
        forceFormatArgsInto(key, locale, new StringBuffer, args).toString()

      def forceFormatValuesInto[L: LocaleMagnet](key: ResourceKey, locale: L, appendTo: StringBuffer, values: Any*): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatValuesInto(buffer, values: _*))

      def forceFormatValues[L: LocaleMagnet](key: ResourceKey, locale: L, values: Any*): String =
        forceFormatValuesInto(key, locale, new StringBuffer, values).toString

      def forceFormatNumericInto[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, num: T, appendTo: StringBuffer): StringBuffer =
        intoBuffer[AlmNumericFormatter](key, appendTo, () ⇒ self.getNumericFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatNumericInto(num, appendTo))

      def forceFormatNumeric[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, num: T): String =
        forceFormatNumericInto(key, locale, num, new StringBuffer).toString()

      def forceFormatNumericRangeInto[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, lower: T, upper: T, appendTo: StringBuffer): StringBuffer =
        intoBuffer[AlmNumericFormatter](key, appendTo, () ⇒ self.getNumericFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatNumericRangeInto(lower, upper, appendTo))

      def forceFormatNumericRange[T: Numeric, L: LocaleMagnet](key: ResourceKey, locale: L, lower: T, upper: T): String =
        forceFormatNumericRangeInto(key, locale, lower, upper, new StringBuffer).toString()

      def forceFormatMeasureInto[L: LocaleMagnet](key: ResourceKey, locale: L, v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
        intoBuffer[AlmMeasureFormatter](key, appendTo, () ⇒ self.getMeasureFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatMeasureInto(v, appendTo, uomSys))

      def forceFormatMeasure[L: LocaleMagnet](key: ResourceKey, locale: L, v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
        forceFormatMeasureInto(key, locale, v, new StringBuffer, uomSys).toString()

      def forceFormatMeasureRangeInto[L: LocaleMagnet](key: ResourceKey, locale: L, lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
        intoBuffer[AlmMeasureFormatter](key, appendTo, () ⇒ self.getMeasureFormatter(key, locale), (formatter, buffer) ⇒ formatter.formatMeasureRangeInto(lower, upper, appendTo, uomSys))

      def forceFormatMeasureRange[L: LocaleMagnet](key: ResourceKey, locale: L, lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
        forceFormatMeasureRangeInto(key, locale, lower, upper, new StringBuffer, uomSys).toString()
    }
  }

}

