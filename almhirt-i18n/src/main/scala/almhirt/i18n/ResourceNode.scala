package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.MessageFormat

trait DirectResourceLookup {
  def locale: ULocale
  def parent: Option[ResourceNode]
  def get(key: ResourceKey): AlmValidation[ResourceValue] = getWithLocale(key).map(_._2)
  def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, ResourceValue)]
  def find(key: ResourceKey): Option[ResourceValue] = findWithLocale(key).map(_._2)
  def findWithLocale(key: ResourceKey): Option[(ULocale, ResourceValue)] = getWithLocale(key).toOption
}

trait ResourceNode extends DirectResourceLookup {
  final def apply(key: ResourceKey): AlmValidation[ResourceValue] = get(key)
  def mappings: Map[ResourceKey, ResourceValue]
  final def withFallbackKeys(fallbackKeys: Map[ResourceKey, ResourceValue]): ResourceNode = {
    val newMappings = fallbackKeys.foldLeft(mappings)({
      case (acc, (fallbackKey, fallbackValue)) ⇒
        acc get fallbackKey match {
          case None ⇒
            acc + (fallbackKey -> fallbackValue)
          case _ ⇒
            acc
        }
    })
    new ResourceNode {
      val locale = ResourceNode.this.locale
      val parent = ResourceNode.this.parent
      def getLocally(key: ResourceKey): AlmValidation[ResourceValue] =
        newMappings get key match {
          case Some(v) ⇒ v.success
          case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
        }
      def mappings = newMappings
    }
  }

  final def withFallback(fallback: ResourceNode): AlmValidation[ResourceNode] =
    if (this.locale.getBaseName == fallback.locale.getBaseName) {
      withFallbackKeys(fallback.mappings).success
    } else {
      ArgumentProblem(s"""Locales do not match: "${this.locale.getBaseName}"(this) differs from "${fallback.locale.getBaseName}"(fallback).""").failure
    }

  override def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, ResourceValue)] =
    getLocally(key).fold(
      fail ⇒ {
        fail match {
          case ResourceNotFoundProblem(_) ⇒
            parent match {
              case Some(p) ⇒
                p.getWithLocale(key)
              case None ⇒
                fail.failure
            }
          case _ ⇒
            fail.failure
        }
      },
      succ ⇒ (this.locale, succ).success)

  def getLocally(key: ResourceKey): AlmValidation[ResourceValue]
}

object ResourceNode {
  import almhirt.xml._
  import almhirt.xml.all._
  def fromXml(xmlElem: Elem, parent: Option[ResourceNode]): AlmValidation[ResourceNode] = ResourceNodeXml.parse(xmlElem, parent)
}

private[almhirt] object ResourceNodeXml {
  import scalaz._, Scalaz._
  import almhirt.almvalidation.kit._
  import almhirt.xml._
  import almhirt.xml.all._
  def parse(xmlElem: Elem, theParent: Option[ResourceNode]): AlmValidation[ResourceNode] = {
    for {
      localeStr ← xmlElem \@! "locale"
      theLocale ← inTryCatch { new ULocale(localeStr) }
      keys ← parseSections(theLocale, xmlElem \\? "section")
    } yield {
      val keysMap = keys.toMap
      new ResourceNode {
        def locale = theLocale
        val parent = theParent
        def getLocally(key: ResourceKey): AlmValidation[ResourceValue] =
          keysMap get key match {
            case Some(v) ⇒ v.success
            case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
          }
        def mappings = keysMap
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
      groups ← parseGroups(locale, elem \\? "group")
    } yield groups.flatMap({ case (groupName, keys) ⇒ keys.map({ case (keyName, value) ⇒ (ResourceKey(checkedName, groupName, keyName), value) }) })
  }

  def parseGroups(locale: ULocale, elems: Seq[Elem]): AlmValidation[Vector[(String, Vector[(String, ResourceValue)])]] = {
    elems.map { elem ⇒ parseGroup(locale, elem).toAgg }.toVector.sequence
  }

  def parseGroup(locale: ULocale, elem: Elem): AlmValidation[(String, Vector[(String, ResourceValue)])] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      keys ← parseKeys(locale, elem \\? "key", "")
      keysFromSections ← parseKeySections(locale, elem \\? "key_section", "")
    } yield (checkedName, keys ++ keysFromSections)
  }

  def parseKeySections(locale: ULocale, elems: Seq[Elem], prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    elems.map { elem ⇒ parseKeySection(locale, elem, prefix).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseKeySection(locale: ULocale, elem: Elem, prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    for {
      newPrefix ← (elem \@? "prefix") match {
        case Some(localPrefix) ⇒ checkName(s"$prefix$localPrefix")
        case None              ⇒ prefix.success
      }
      keys ← parseKeys(locale, elem \\? "key", newPrefix)
      keysFromSections ← parseKeySections(locale, elem \\? "key_section", newPrefix)
    } yield keys ++ keysFromSections
  }

  def parseKeys(locale: ULocale, elems: Seq[Elem], prefix: String): AlmValidation[Vector[(String, ResourceValue)]] = {
    elems.map { elem ⇒ parseKey(locale, elem, prefix).toAgg }.toVector.sequence
  }

  val stringValueBasedDescriptors = Set("", "plain", "icu")
  def parseKey(locale: ULocale, elem: Elem, prefix: String): AlmValidation[(String, ResourceValue)] = {
    for {
      name ← elem \@! "name"
      checkedName ← checkName(name)
      elemTypeDescriptor ← ((elem \@? "type") getOrElse "").success
      value ← if (stringValueBasedDescriptors(elemTypeDescriptor)) {
        for {
          valueElem ← elem \! "value"
          v ← parseStringValueBasedValue(locale, valueElem, elemTypeDescriptor)
        } yield v
      } else if (elemTypeDescriptor == "measured-value") {
        parseMeasureFormatterValue(locale, elem)
      } else if (elemTypeDescriptor == "boolean-value") {
        parseBooleanFormatterValue(locale, elem)
      } else {
        ArgumentProblem(s""""$elemTypeDescriptor" is not a valid type for a resource value.""").failure
      }
    } yield (s"$prefix$checkedName", value)
  }

  def parseStringValueBasedValue(locale: ULocale, valueElem: Elem, typeDescriptor: String): AlmValidation[ResourceValue] =
    for {
      valueStr ← valueElem.text.notEmptyOrWhitespace().map(_.replaceAll("\\s{2,}", " ").trim())
      value ← typeDescriptor match {
        case ""      ⇒ RawStringValue(valueStr).success
        case "plain" ⇒ RawStringValue(valueStr).success
        case "icu"   ⇒ IcuMessageFormat(valueStr, locale)
        case x       ⇒ ArgumentProblem(s""""$x" is not a valid type for a resource value.""").failure
      }
    } yield value

  def parseMeasureFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    def parseFormatDefinition(format: Elem): AlmValidation[impl.MeasuredValueFormatterBuilder.FormatDefinition] =
      for {
        uomName ← (format \! "unit-of-measurement").map(_.text)
        uom ← UnitsOfMeasurement.byName(uomName)
        minFractionDigits ← (format \? "min-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
        maxFractionDigits ← (format \? "max-fraction-digits").flatMap(e ⇒ e.map { _.text.toIntAlm }.validationOut)
        useDigitGroup ← (format \? "use-digit-groups").flatMap(e ⇒ e.map { _.text.toBooleanAlm }.validationOut)
      } yield impl.MeasuredValueFormatterBuilder.FormatDefinition(uom, minFractionDigits, maxFractionDigits, useDigitGroup)

    val paramNameV = for {
      theOnlyParamNameElem ← (elem \? "parameter-name")
      definedParamName ← theOnlyParamNameElem.map(_.text.notEmptyOrWhitespace()).validationOut
      fallbackName ← (elem \@! "name")
    } yield definedParamName getOrElse fallbackName

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
      formatter ← impl.MeasuredValueFormatterBuilder(impl.MeasuredValueFormatterBuilder.CtorParams(locale, paramName, formatWidth, defaultDefinition, specificFormats.toMap))
    } yield formatter
  }

  def parseBooleanFormatterValue(locale: ULocale, elem: Elem): AlmValidation[ResourceValue] = {
    val paramNameV = for {
      theOnlyParamNameElem ← (elem \? "parameter-name")
      definedParamName ← theOnlyParamNameElem.map(_.text.notEmptyOrWhitespace()).validationOut
      fallbackName ← (elem \@! "name")
    } yield definedParamName getOrElse fallbackName

    for {
      paramName ← paramNameV
      trueText ← (elem \? "true-text").flatMap(e => e.text.notEmptyOrWhitespace().map(_.replaceAll("\\s{2,}", " ").trim()))
    } yield impl.BooleanValueFormatterBuilder(locale, paramName, "", "")
  }

  def checkName(name: String): AlmValidation[String] =
    if (name.contains('.') || name.contains(' ')) {
      ArgumentProblem(s""""$name" is not allowed. An key part may not conatain a dot or whitespace.""").failure
    } else {
      name.notEmptyOrWhitespace()
    }

}