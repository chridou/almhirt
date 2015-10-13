package almhirt.i18n

import java.text.FieldPosition
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.MessageFormat

/**
 * Basic function to retrieve resources for a locale
 *
 * IMPORTANT!
 * Whoever mixes in this trait must override get, getWithLocale or both to prevent a stack overflow.
 */
trait PinnedResourceLookup extends Function1[ResourceKey, AlmValidation[ResourceValue]] {
  final def apply(key: ResourceKey): AlmValidation[ResourceValue] = get(key)

  /**
   * @return the locale for all resources
   */
  def locale: ULocale

  /**
   * Get a resource key
   *
   * @param key the [[almhirt.common.ResourceKey]] to use for the lookup
   * @return the resource for the key or an error
   */
  def get(key: ResourceKey): AlmValidation[ResourceValue] = getWithLocale(key).map(_._2)

  /**
   * Get a resource key and the locale
   *
   * @param key the [[almhirt.common.ResourceKey]] to use for the lookup
   * @return the resource for the key and the locale or an error
   */
  def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, ResourceValue)] = get(key).map { (locale, _) }

  /**
   * Find a resource key
   *
   * @param key the [[almhirt.common.ResourceKey]] to use for the lookup
   * @return the found Resource for the key or none
   */
  def find(key: ResourceKey): Option[ResourceValue] = get(key).toOption

  /**
   * Find a resource key and the locale
   *
   * @param key the [[almhirt.common.ResourceKey]] to use for the lookup
   * @return the found Resource for the key and the locale or none
   */
  def findWithLocale(key: ResourceKey): Option[(ULocale, ResourceValue)] = getWithLocale(key).toOption

  /**
   * Get an [[AlmFormatter]]
   *
   * @param key the [[ResourceKey]] for the queried [[AlmFormatter]]
   * @return the possibly found [[AlmFormatter]]
   */
  final def getFormatter(key: ResourceKey): AlmValidation[AlmFormatter] =
    for {
      res ← get(key)
      fmt ← res match {
        case fmt: IcuResourceValue ⇒
          new IcuFormatter(fmt.formatInstance).success
        case raw: RawStringResourceValue ⇒
          raw.success
        case f: BasicValueResourceValue ⇒
          f.formatable.success
      }
    } yield fmt

  /**
   * Get an [[AlmNumericFormatter]]
   *
   * @param key the [[ResourceKey]] for the queried [[AlmNumericFormatter]]
   * @return the possibly found [[AlmNumericFormatter]]
   */
  final def getNumericFormatter(key: ResourceKey): AlmValidation[AlmNumericFormatter] =
    for {
      res ← get(key)
      fmt ← res match {
        case f: NumericValueResourceValue ⇒
          f.formatable.success
        case x ⇒
          ArgumentProblem(s"""Value at key "$key" is not a numeric formatter.""").failure

      }
    } yield fmt

  /**
   * Get an [[AlmMeasureFormatter]]
   *
   * @param key the [[ResourceKey]] for the queried [[AlmMeasureFormatter]]
   * @return the possibly found [[AlmMeasureFormatter]]
   */
  final def getMeasureFormatter(key: ResourceKey): AlmValidation[AlmMeasureFormatter] =
    for {
      res ← get(key)
      fmt ← res match {
        case f: MeasuredValueResourceValue ⇒
          f.formatable.success
        case x ⇒
          ArgumentProblem(s"""Value at key "$key" is not a measure formatter.""").failure
      }
    } yield fmt

  /**
   * Get a String
   *
   * @param key the [[ResourceKey]] for the queried String
   * @return the possibly found String
   */
  final def getRawText(key: ResourceKey): AlmValidation[String] =
    for {
      res ← get(key)
      str ← res match {
        case fmt: IcuResourceValue ⇒
          inTryCatch { fmt.raw }
        case r: RawStringResourceValue ⇒
          r.raw.success
        case _: BasicValueResourceValue ⇒
          ArgumentProblem(s"""Value at key "$key" does not have a direct String representation so there is no direct renderable.""").failure
      }
    } yield str

  final def getTextResource(key: ResourceKey): AlmValidation[TextResourceValue] =
    get(key).flatMap {
      case res: TextResourceValue ⇒ res.success
      case _                      ⇒ ArgumentProblem(s"Key $key does not map to a text resource.").failure
    }
}

object PinnedResourceLookup {
  implicit class PinnedResourceLookupBasicOps(val self: PinnedResourceLookup) extends AnyVal {
    def findTextResource(key: ResourceKey): Option[TextResourceValue] = self.getTextResource(key).toOption

    def forceFormatter[T](key: ResourceKey): AlmFormatter =
      self.getFormatter(key).resultOrEscalate

    def rawText(key: ResourceKey): String =
      self.getRawText(key) fold (
        fail ⇒ s"{$key: ${fail.message}}",
        succ ⇒ succ)
  }

  object SafeFormattingImplicits {
    implicit class PinnedResourceLookupSafeFormatterOps(val self: PinnedResourceLookup) extends AnyVal {
      def formatIntoBuffer(key: ResourceKey, appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.formatInto(appendTo, args: _*)
        } yield res

      def formatArgsIntoBuffer(key: ResourceKey, appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.formatArgsInto(appendTo, args)
        } yield res

      def format(key: ResourceKey, args: (String, Any)*): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.format(args: _*)
        } yield res

      def formatArgs(key: ResourceKey, args: Map[String, Any]): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.formatArgs(args)
        } yield res

      def formatValuesInto(key: ResourceKey, appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.formatValuesInto(appendTo, values: _*)
        } yield res

      def formatValues(key: ResourceKey, values: Any*): AlmValidation[String] =
        for {
          formatable ← self.getFormatter(key)
          res ← formatable.formatValues(values: _*)
        } yield res

      def formatNumericIntoAt[T: Numeric](key: ResourceKey, num: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getNumericFormatter(key)
          res ← formatable.formatNumericIntoAt(num, appendTo, pos)
        } yield res

      def formatNumericInto[T: Numeric](key: ResourceKey, num: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
        formatNumericIntoAt(key, num, appendTo, util.DontCareFieldPosition)

      def formatNumeric[T: Numeric](key: ResourceKey, num: T): AlmValidation[String] =
        formatNumericIntoAt(key, num, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())

      def formatNumericRangeIntoAt[T: Numeric](key: ResourceKey, lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getNumericFormatter(key)
          res ← formatable.formatNumericRangeIntoAt(upper, lower, appendTo, pos)
        } yield res

      def formatNumericRangeInto[T: Numeric](key: ResourceKey, lower: T, upper: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
        formatNumericRangeIntoAt(key, lower, upper, appendTo, util.DontCareFieldPosition)

      def formatNumericRange[T: Numeric](key: ResourceKey, lower: T, upper: T): AlmValidation[String] =
        formatNumericRangeIntoAt(key, lower, upper, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())

      def formatMeasureIntoAt(key: ResourceKey, v: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getMeasureFormatter(key)
          res ← formatable.formatMeasureIntoAt(v, appendTo, pos, uomSys)
        } yield res

      def formatMeasureInto(key: ResourceKey, v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        formatMeasureIntoAt(key, v, appendTo, util.DontCareFieldPosition, uomSys)

      def formatMeasure(key: ResourceKey, v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
        formatMeasureIntoAt(key, v, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())

      def formatMeasureRangeIntoAt(key: ResourceKey, lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        for {
          formatable ← self.getMeasureFormatter(key)
          res ← formatable.formatMeasureRangeIntoAt(lower, upper, appendTo, pos, uomSys)
        } yield res

      def formatMeasureRangeInto(key: ResourceKey, lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
        formatMeasureRangeIntoAt(key, lower, upper, appendTo, util.DontCareFieldPosition, uomSys)

      def formatMeasureRange(key: ResourceKey, lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
        formatMeasureRangeIntoAt(key, lower, upper, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())
    }
  }

  object UnsafeFormattingImplicits {
    private def intoBuffer[T](key: ResourceKey, buffer: StringBuffer, f1: () ⇒ AlmValidation[T], f2: (T, StringBuffer) ⇒ AlmValidation[StringBuffer]): StringBuffer =
      f1() fold (
        fail ⇒ buffer.append(s"{$key}"),
        succ1 ⇒ f2(succ1, buffer) fold (
          fail ⇒ buffer.append(s"{$key: ${fail.message}}"),
          succ2 ⇒ succ2))

    implicit class PinnedResourceLookupUnsafeFormatterOps(val self: PinnedResourceLookup) extends AnyVal {
      def forceFormatInto(key: ResourceKey, appendTo: StringBuffer, args: (String, Any)*): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key), (formatter, buffer) ⇒ formatter.formatInto(buffer, args: _*))

      def forceFormatArgsInto(key: ResourceKey, appendTo: StringBuffer, args: Map[String, Any]): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key), (formatter, buffer) ⇒ formatter.formatArgsInto(buffer, args))

      def forceFormat(key: ResourceKey, args: (String, Any)*): String =
        forceFormatInto(key, new StringBuffer, args: _*).toString()

      def forceFormatArgs(key: ResourceKey, args: Map[String, Any]): String =
        forceFormatArgsInto(key, new StringBuffer, args).toString()

      def forceFormatValuesInto(key: ResourceKey, appendTo: StringBuffer, values: Any*): StringBuffer =
        intoBuffer[AlmFormatter](key, appendTo, () ⇒ self.getFormatter(key), (formatter, buffer) ⇒ formatter.formatValuesInto(buffer, values: _*))

      def forceFormatValues(key: ResourceKey, values: Any*): String =
        forceFormatValuesInto(key, new StringBuffer, values).toString

      def forceFormatNumericInto[T: Numeric](key: ResourceKey, num: T, appendTo: StringBuffer): StringBuffer =
        intoBuffer[AlmNumericFormatter](key, appendTo, () ⇒ self.getNumericFormatter(key), (formatter, buffer) ⇒ formatter.formatNumericInto(num, appendTo))

      def forceFormatNumeric[T: Numeric](key: ResourceKey, num: T): String =
        forceFormatNumericInto(key, num, new StringBuffer).toString()

      def forceFormatNumericRangeInto[T: Numeric](key: ResourceKey, lower: T, upper: T, appendTo: StringBuffer): StringBuffer =
        intoBuffer[AlmNumericFormatter](key, appendTo, () ⇒ self.getNumericFormatter(key), (formatter, buffer) ⇒ formatter.formatNumericRangeInto(lower, upper, appendTo))

      def forceFormatNumericRange[T: Numeric](key: ResourceKey, lower: T, upper: T): String =
        forceFormatNumericRangeInto(key, lower, upper, new StringBuffer).toString()

      def forceFormatMeasureInto(key: ResourceKey, v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
        intoBuffer[AlmMeasureFormatter](key, appendTo, () ⇒ self.getMeasureFormatter(key), (formatter, buffer) ⇒ formatter.formatMeasureInto(v, appendTo, uomSys))

      def forceFormatMeasure(key: ResourceKey, v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
        forceFormatMeasureInto(key, v, new StringBuffer, uomSys).toString()

      def forceFormatMeasureRangeInto(key: ResourceKey, lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
        intoBuffer[AlmMeasureFormatter](key, appendTo, () ⇒ self.getMeasureFormatter(key), (formatter, buffer) ⇒ formatter.formatMeasureRangeInto(lower, upper, appendTo, uomSys))

      def forceFormatMeasureRange(key: ResourceKey, lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
        forceFormatMeasureRangeInto(key, lower, upper, new StringBuffer, uomSys).toString()
    }
  }
}

trait PinnedResources extends PinnedResourceLookup {
  def mappings: Map[ResourceKey, ResourceValue]

  /**
   * Get all keys in this lookup under the specified section.
   *
   * Warning! Calling this function may be very expensive.
   *
   * @param section the resource section to get the keys from
   * @return all resource keys in the specified section
   */
  def mappingsInSection(section: ResourceSection): Map[ResourceKey, ResourceValue] = mappings.filterKeys { _.section == section.section }

  /**
   * Get all keys in this lookup under the specified group.
   *
   * Warning! Calling this function may be very expensive.
   *
   * @param group the resource group to get the keys from
   * @return all resource keys in the specified group
   */
  def mappingsInGroup(group: ResourceGroup): Map[ResourceKey, ResourceValue] = mappings.filterKeys { key ⇒ key.section == group.section && key.group == group.group }

  final def withFallbackKeys(fallbackKeys: Map[ResourceKey, ResourceValue]): PinnedResources = {
    val newMappings = fallbackKeys.foldLeft(mappings)({
      case (acc, (fallbackKey, fallbackValue)) ⇒
        acc get fallbackKey match {
          case None ⇒
            acc + (fallbackKey -> fallbackValue)
          case _ ⇒
            acc
        }
    })
    new PinnedResources {
      val locale = PinnedResources.this.locale
      override def get(key: ResourceKey): AlmValidation[ResourceValue] =
        newMappings get key match {
          case Some(v) ⇒ v.success
          case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
        }
      override def mappings = newMappings
    }
  }

  final def withFallback(fallback: PinnedResources): AlmValidation[PinnedResources] =
    if (this.locale.getBaseName == fallback.locale.getBaseName) {
      withFallbackKeys(fallback.mappings).success
    } else {
      ArgumentProblem(s"""Locales do not match: "${this.locale.getBaseName}"(this) differs from "${fallback.locale.getBaseName}"(fallback).""").failure
    }
}

object PinnedResources {
  import almhirt.xml._
  import almhirt.xml.all._

  def apply(theLocale: ULocale, keysMap: Map[ResourceKey, ResourceValue]): PinnedResources = {
    new PinnedResources {
      override val locale = theLocale
      override def get(key: ResourceKey): AlmValidation[ResourceValue] =
        keysMap get key match {
          case Some(v) ⇒ v.success
          case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
        }
      override val mappings = keysMap
    }
  }

  def fromXml(xmlElem: Elem): AlmValidation[PinnedResources] = ResourceNodeXml.parse(xmlElem)
}

private[almhirt] object ResourceNodeXml {
  import scalaz._, Scalaz._
  import almhirt.almvalidation.kit._
  import almhirt.xml._
  import almhirt.xml.all._
  def parse(xmlElem: Elem): AlmValidation[PinnedResources] = {
    for {
      localeStr ← xmlElem \@! "locale"
      theLocale ← inTryCatch { new ULocale(localeStr) }
      keys ← parseSections(theLocale, xmlElem \\? "section").leftMap(p ⇒ UnspecifiedProblem(s"""Problem in resources for locale ${theLocale.toLanguageTag}.""", cause = Some(p)))
    } yield {
      val keysMap = keys.toMap
      PinnedResources(theLocale, keysMap)
    }
  }

  def parseSections(locale: ULocale, elems: Seq[Elem]): AlmValidation[Vector[(ResourceKey, ResourceValue)]] = {
    elems.map { elem ⇒ parseSection(locale, elem).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseSection(locale: ULocale, elem: Elem): AlmValidation[Vector[(ResourceKey, ResourceValue)]] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      groups ← parseGroups(locale, elem \\? "group").leftMap(p ⇒ UnspecifiedProblem(s"""Problem in section "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
    } yield groups.flatMap({ case (groupName, keys) ⇒ keys.map({ case (keyName, value) ⇒ (ResourceKey(checkedName, groupName, keyName), value) }) })
  }

  def parseGroups(locale: ULocale, elems: Seq[Elem]): AlmValidation[Vector[(String, Vector[(String, ResourceValue)])]] = {
    elems.map { elem ⇒ parseGroup(locale, elem).toAgg }.toVector.sequence
  }

  def parseGroup(locale: ULocale, elem: Elem): AlmValidation[(String, Vector[(String, ResourceValue)])] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      keys ← parseKeys(locale, elem \\? "key", "").leftMap(p ⇒ UnspecifiedProblem(s"""Problem in group "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
      keysFromSections ← parseKeySections(locale, elem \\? "key-section", "").leftMap(p ⇒ UnspecifiedProblem(s"""Problem in group "$checkedName"(in a key-section) for locale ${locale.toLanguageTag}.""", cause = Some(p)))
    } yield (checkedName, keys ++ keysFromSections)
  }

  def parseKeySections(locale: ULocale, elems: Seq[Elem], prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    elems.map { elem ⇒ parseKeySection(locale, elem, prefix).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseKeySection(locale: ULocale, elem: Elem, prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    (for {
      newPrefix ← (elem \@? "prefix") match {
        case Some(localPrefix) ⇒ checkName(s"$prefix$localPrefix")
        case None              ⇒ prefix.success
      }
      keys ← parseKeys(locale, elem \\? "key", newPrefix)
      keysFromSections ← parseKeySections(locale, elem \\? "key-section", newPrefix)
    } yield keys ++ keysFromSections).leftMap(p ⇒ UnspecifiedProblem(s"""Problem in key-section.""", cause = Some(p)))
  }

  def parseKeys(locale: ULocale, elems: Seq[Elem], prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    elems.map { elem ⇒ parseKey(locale, elem, prefix).toAgg }.toVector.sequence
  }

  val stringValueBasedDescriptors = Set("plain", "icu")
  def parseKey(locale: ULocale, elem: Elem, prefix: String): AlmValidation[(String, ResourceValue)] = {
    (for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      elemFormatterElem ← elem.firstChildNodeExcluding("comment")
      value ← {
        val typeDescriptor = elemFormatterElem.label
        if (stringValueBasedDescriptors(typeDescriptor)) {
          parseStringValueBasedValue(locale, elemFormatterElem, typeDescriptor)
        } else if (typeDescriptor == "number") {
          parseNumberFormatterValue(locale, elemFormatterElem)
        } else if (typeDescriptor == "measured-value") {
          parseMeasureFormatterValue(locale, elemFormatterElem)
        } else if (typeDescriptor == "boolean-value") {
          parseBooleanFormatterValue(locale, elemFormatterElem)
        } else if (typeDescriptor == "select-text") {
          parseSelectTextFormatterValue(locale, elemFormatterElem)
        } else if (typeDescriptor == "selection-of-many") {
          parseSelectionFromManyFormatterValue(locale, elemFormatterElem)
        } else {
          ArgumentProblem(s""""$typeDescriptor" is not a valid type for a resource value.""").failure
        }
      }.leftMap(p ⇒ UnspecifiedProblem(s"""Problem with key "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
    } yield (s"$prefix$checkedName", value))
  }

  private def trimText(text: String): AlmValidation[String] =
    text.replaceAll("\\s{2,}", " ").trim().notEmptyOrWhitespace()

  def parseStringValueBasedValue(locale: ULocale, valueElem: Elem, typeDescriptor: String): AlmValidation[ResourceValue] =
    for {
      valueStr ← trimText(valueElem.text)
      value ← typeDescriptor match {
        case ""      ⇒ RawStringResourceValue(locale, valueStr).success
        case "plain" ⇒ RawStringResourceValue(locale, valueStr).success
        case "icu"   ⇒ IcuResourceValue(valueStr, locale)
        case x       ⇒ ArgumentProblem(s""""$x" is not a valid type for a resource value.""").failure
      }
    } yield value

  def parseMeasureFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    def parseFormatDefinition(format: Elem): AlmValidation[impl.MeasuredFormatResourceValue.FormatDefinition] =
      for {
        uomName ← (format \! "unit-of-measurement").map(_.text)
        uom ← UnitsOfMeasurement.byName(uomName)
        minFractionDigits ← (format \? "min-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
        maxFractionDigits ← (format \? "max-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
        useDigitGroup ← (format \? "use-digit-groups").flatMap(e ⇒ e.map { _.text.toBooleanAlm }.validationOut)
        rangeSeparator ← (elem \? "range-separator").map(e ⇒ e.map(_.text))
      } yield impl.MeasuredFormatResourceValue.FormatDefinition(uom, minFractionDigits, maxFractionDigits, useDigitGroup, rangeSeparator)

    val paramNameV = for {
      theOnlyParamNameStr ← elem \@! "parameter"
      definedParamName ← theOnlyParamNameStr.trim().notEmptyOrWhitespace()
    } yield definedParamName

    for {
      paramName ← paramNameV
      formatWidth ← (elem \? "format-width").flatMap(e ⇒ e.map { e ⇒ MeasureRenderWidth.parseString(e.text) }.validationOut)
      defaultDefinitionElem ← (elem \! "default-format")
      defaultDefinition ← parseFormatDefinition(defaultDefinitionElem)
      specificsElem ← elem \? "selective"
      specificFormats ← specificsElem.toVector.flatMap { spe ⇒ spe \\? "format" }.map { elem ⇒
        (for {
          uomSysElem ← (elem \! "system")
          uomSys ← UnitsOfMeasurementSystem.parseString(uomSysElem.text)
          formatDefinition ← parseFormatDefinition(elem)
        } yield (uomSys, formatDefinition)).toAgg
      }.sequence
      formatter ← impl.MeasuredFormatResourceValue(impl.MeasuredFormatResourceValue.CtorParams(locale, paramName, formatWidth, defaultDefinition, specificFormats.toMap))
    } yield formatter
  }

  def parseNumberFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {

    val paramNameV = for {
      theOnlyParamNameStr ← elem \@! "parameter"
      definedParamName ← theOnlyParamNameStr.trim().notEmptyOrWhitespace()
    } yield definedParamName

    for {
      paramName ← paramNameV
      style ← (elem \? "style").flatMap(e ⇒ e.map { e ⇒ NumberFormatStyle.parse(e.text) }.validationOut)
      minFractionDigits ← (elem \? "min-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
      maxFractionDigits ← (elem \? "max-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
      useDigitsGrouping ← (elem \? "use-digit-groups").flatMap(e ⇒ e.map { _.text.toBooleanAlm }.validationOut)
      rangeSeparator ← (elem \? "range-separator").map(e ⇒ e.map(_.text))
      formatter ← impl.NumberFormatResourceValue.apply(locale, paramName, style, minFractionDigits, maxFractionDigits, useDigitsGrouping, rangeSeparator)
    } yield formatter
  }

  def parseBooleanFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    val paramNameV = for {
      theOnlyParamNameStr ← elem \@! "parameter"
      definedParamName ← theOnlyParamNameStr.trim().notEmptyOrWhitespace()
    } yield definedParamName

    for {
      paramName ← paramNameV
      trueTextElem ← (elem \? "true-text")
      trueText ← trueTextElem.map(e ⇒ trimText(e.text)).validationOut()
      falseTextElem ← (elem \? "false-text")
      falseText ← falseTextElem.map(e ⇒ trimText(e.text)).validationOut()
    } yield impl.BooleanFormatResourceValue(locale, paramName, trueText getOrElse "", falseText getOrElse "")
  }

  def parseSelectTextFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    val paramNameV = for {
      theOnlyParamNameStr ← elem \@! "parameter"
      definedParamName ← theOnlyParamNameStr.trim().notEmptyOrWhitespace()
    } yield definedParamName

    for {
      paramName ← paramNameV
      defaultElem ← (elem \? "defaut")
      defaultText ← defaultElem.map(e ⇒ trimText(e.text)).validationOut()
      selectItems ← (elem \\? "select").map { e ⇒
        (for {
          selector ← (e \@! "selector")
        } yield (selector, e.text.replaceAll("\\s{2,}", " ").trim())).toAgg
      }.toVector.sequence
    } yield impl.SelectTextFormatResourceValue(locale, paramName, defaultText, selectItems.toMap)
  }

  def parseSelectionFromManyFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    import almhirt.i18n.impl.SelectionOfManyResourceValue
    def getParameterValueOpt(elem: Elem, name: String): AlmValidation[Option[String]] =
      (elem \@? name).map(_.trim().notEmptyOrWhitespace()).validationOut

    for {
      selectionSizeParameter ← getParameterValueOpt(elem, "selection-size-parameter")
      lowerIndexParameter ← getParameterValueOpt(elem, "lower-index-parameter")
      allItemsCountParameter ← getParameterValueOpt(elem, "all-items-count-parameter")
      upperIndexParameter ← getParameterValueOpt(elem, "upper-index-parameter")
      ifAllItemsCountParamIsZeroElem ← (elem \! "if-all-items-count-is-zero")
      ifAllItemsCountParamIsZero ← trimText(ifAllItemsCountParamIsZeroElem.text)
      ifSelectionSizeIsZeroElem ← (elem \! "if-selection-size-is-zero")
      ifSelectionSizeIsZero ← trimText(ifSelectionSizeIsZeroElem.text)
      separatorElemOpt ← (elem \? "separator")
      embedSeperatorInSpaces ← separatorElemOpt.map(elem ⇒
        getParameterValueOpt(elem, "embed-in-spaces").flatMap(v ⇒
          v.map(_.toBooleanAlm).validationOut)).validationOut.map(_.flatten.getOrElse(false)) 
      preSeperatorTextOpt <- separatorElemOpt.map(elem => trimText(elem.text)).validationOut
      seperatorOpt <- (if(embedSeperatorInSpaces) preSeperatorTextOpt.map(txt => s" $txt ") else preSeperatorTextOpt).success
    } yield new SelectionOfManyResourceValue(
      locale = locale,
      selectionSizeParameter = selectionSizeParameter,
      lowerIndexParameter = lowerIndexParameter,
      allItemsCountParameter = allItemsCountParameter,
      upperIndexParameter = upperIndexParameter,
      ifAllItemsCountParamIsZero = ifAllItemsCountParamIsZero,
      ifSelectionSizeIsZero = ifSelectionSizeIsZero,
      separator = seperatorOpt,
      rangeSelectionFormatter = None,
      amountSelectionFormatter = None,
      allItemsPartFormatter = None)
  }

  def checkName(name: String): AlmValidation[String] =
    if (name.contains('.') || name.contains(' ')) {
      ArgumentProblem(s""""$name" is not allowed. An key part may not conatain a dot or whitespace.""").failure
    } else {
      name.notEmptyOrWhitespace()
    }

}