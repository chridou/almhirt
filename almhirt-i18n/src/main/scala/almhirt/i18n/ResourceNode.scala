package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.MessageFormat

trait DirectResourceLookup {
  def locale: ULocale
  def get(key: ResourceKey): AlmValidation[ResourceValue] = getWithLocale(key).map(_._2)
  def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, ResourceValue)]
  final def find(key: ResourceKey): Option[ResourceValue] = findWithLocale(key).map(_._2)
  final def findWithLocale(key: ResourceKey): Option[(ULocale, ResourceValue)] = getWithLocale(key).toOption
}

trait ResourceNode extends DirectResourceLookup {
  final def apply(key: ResourceKey): AlmValidation[ResourceValue] = get(key)
  def parent: Option[ResourceNode]
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
  trait KeyItem
  final case class RawStringContainerItem(value: String) extends KeyItem
  final case class IcuMessageFormatContainer(value: String) extends KeyItem

  import scalaz._, Scalaz._
  import almhirt.almvalidation.kit._
  import almhirt.xml._
  import almhirt.xml.all._
  def parse(xmlElem: Elem, theParent: Option[ResourceNode]): AlmValidation[ResourceNode] = {
    for {
      localeStr ← xmlElem \@! "locale"
      theLocale ← inTryCatch { new ULocale(localeStr) }
      keysRaw ← parseSections(xmlElem \\? "section")
      keys ← keysRaw.map({
        case (k, RawStringContainerItem(v))    ⇒ ((k, RawStringValue(v)).success[Problem]).toAgg
        case (k, IcuMessageFormatContainer(v)) ⇒ IcuMessageFormat(v, theLocale).map((k, (_: ResourceValue))).toAgg
      }).sequence
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

  def parseSections(elems: Seq[Elem]): AlmValidation[Vector[(ResourceKey, KeyItem)]] = {
    elems.map { elem ⇒ parseSection(elem).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseSection(elem: Elem): AlmValidation[Vector[(ResourceKey, KeyItem)]] = {
    for {
      name ← elem \@! "name"
      groups ← parseGroups(elem \\? "group")
    } yield groups.flatMap({ case (groupName, keys) ⇒ keys.map({ case (keyName, value) ⇒ (ResourceKey(name, groupName, keyName), value) }) })
  }

  def parseGroups(elems: Seq[Elem]): AlmValidation[Vector[(String, Vector[(String, KeyItem)])]] = {
    elems.map { elem ⇒ parseGroup(elem).toAgg }.toVector.sequence
  }

  def parseGroup(elem: Elem): AlmValidation[(String, Vector[(String, KeyItem)])] = {
    for {
      name ← elem \@! "name"
      keys ← parseKeys(elem \\? "key")
    } yield (name, keys)
  }

  def parseKeys(elems: Seq[Elem]): AlmValidation[Vector[(String, KeyItem)]] = {
    elems.map { elem ⇒ parseKey(elem).toAgg }.toVector.sequence
  }

  def parseKey(elem: Elem): AlmValidation[(String, KeyItem)] = {
    for {
      name ← elem \@! "name"
      valueStr ← elem.text.success
      value ← elem \@? "type" match {
        case None        ⇒ RawStringContainerItem(valueStr).success
        case Some("icu") ⇒ IcuMessageFormatContainer(valueStr).success
        case Some(x)     ⇒ ArgumentProblem(s""""$x" is not a valid type for a resource value.""").failure
      }
    } yield (name, value)
  }

}