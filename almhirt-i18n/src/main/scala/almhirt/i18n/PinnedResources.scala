package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
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
}

trait PinnedResources extends PinnedResourceLookup {
  def mappings: Map[ResourceKey, ResourceValue]

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
      def mappings = newMappings
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
      keys ← parseSections(theLocale, xmlElem \\? "section").leftMap(p => UnspecifiedProblem(s"""Problem in resources for locale ${theLocale.toLanguageTag}.""", cause = Some(p)))
    } yield {
      val keysMap = keys.toMap
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
  }

  def parseSections(locale: ULocale, elems: Seq[Elem]): AlmValidation[Vector[(ResourceKey, ResourceValue)]] = {
    elems.map { elem ⇒ parseSection(locale, elem).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseSection(locale: ULocale, elem: Elem): AlmValidation[Vector[(ResourceKey, ResourceValue)]] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      groups ← parseGroups(locale, elem \\? "group").leftMap(p => UnspecifiedProblem(s"""Problem in section "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
    } yield groups.flatMap({ case (groupName, keys) ⇒ keys.map({ case (keyName, value) ⇒ (ResourceKey(checkedName, groupName, keyName), value) }) })
  }

  def parseGroups(locale: ULocale, elems: Seq[Elem]): AlmValidation[Vector[(String, Vector[(String, ResourceValue)])]] = {
    elems.map { elem ⇒ parseGroup(locale, elem).toAgg }.toVector.sequence
  }

  def parseGroup(locale: ULocale, elem: Elem): AlmValidation[(String, Vector[(String, ResourceValue)])] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      keys ← parseKeys(locale, elem \\? "key", "").leftMap(p => UnspecifiedProblem(s"""Problem in group "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
      keysFromSections ← parseKeySections(locale, elem \\? "key-section", "").leftMap(p => UnspecifiedProblem(s"""Problem in group "$checkedName"(in a key-section) for locale ${locale.toLanguageTag}.""", cause = Some(p)))
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
    } yield keys ++ keysFromSections).leftMap(p => UnspecifiedProblem(s"""Problem in key-section.""", cause = Some(p)))
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
        } else {
          ArgumentProblem(s""""$typeDescriptor" is not a valid type for a resource value.""").failure
        }
      }.leftMap(p => UnspecifiedProblem(s"""Problem with key "$checkedName" for locale ${locale.toLanguageTag}.""", cause = Some(p)))
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

  def checkName(name: String): AlmValidation[String] =
    if (name.contains('.') || name.contains(' ')) {
      ArgumentProblem(s""""$name" is not allowed. An key part may not conatain a dot or whitespace.""").failure
    } else {
      name.notEmptyOrWhitespace()
    }

}