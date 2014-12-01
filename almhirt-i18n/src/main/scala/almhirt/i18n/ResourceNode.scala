package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait DirectResourceLookup {
  def locale: ULocale
  def get(key: ResourceKey): AlmValidation[String] = getWithLocale(key).map(_._2)
  def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, String)]
  final def find(key: ResourceKey): Option[String] = findWithLocale(key).map(_._2)
  final def findWithLocale(key: ResourceKey): Option[(ULocale, String)] = getWithLocale(key).toOption
}

trait ResourceNode extends DirectResourceLookup {
  final def apply(key: ResourceKey): AlmValidation[String] = get(key)
  def parent: Option[ResourceNode]
  def mappings: Map[ResourceKey, String]
  final def withFallbackKeys(fallbackKeys: Map[ResourceKey, String]): ResourceNode = {
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
      def getLocally(key: ResourceKey): AlmValidation[String] =
        newMappings get key match {
          case Some(v) ⇒ v.success
          case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
        }
      def mappings = newMappings
    }
  }

  final def withFallback(fallback: ResourceNode): AlmValidation[ResourceNode] = 
    if(this.locale.getBaseName == fallback.locale.getBaseName) {
      withFallbackKeys(fallback.mappings).success
    } else {
      ArgumentProblem(s"""Locales do not match: "${this.locale.getBaseName}"(this) differs from "${fallback.locale.getBaseName}"(fallback).""").failure
    }
  
  override def getWithLocale(key: ResourceKey): AlmValidation[(ULocale, String)] =
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

  def getLocally(key: ResourceKey): AlmValidation[String]
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
      keys ← parseSections(xmlElem \\? "section")
    } yield {
      val keysMap = keys.toMap
      new ResourceNode {
        def locale = theLocale
        val parent = theParent
        def getLocally(key: ResourceKey): AlmValidation[String] =
          keysMap get key match {
            case Some(v) ⇒ v.success
            case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
          }
        def mappings = keysMap
      }
    }
  }

  def parseSections(elems: Seq[Elem]): AlmValidation[Vector[(ResourceKey, String)]] = {
    elems.map { elem ⇒ parseSection(elem).toAgg }.toVector.sequence.map(_.flatten)
  }

  def parseSection(elem: Elem): AlmValidation[Vector[(ResourceKey, String)]] = {
    for {
      name ← elem \@! "name"
      groups ← parseGroups(elem \\? "group")
    } yield groups.flatMap({ case (groupName, keys) ⇒ keys.map({ case (keyName, value) ⇒ (ResourceKey(name, groupName, keyName), value) }) })
  }

  def parseGroups(elems: Seq[Elem]): AlmValidation[Vector[(String, Vector[(String, String)])]] = {
    elems.map { elem ⇒ parseGroup(elem).toAgg }.toVector.sequence
  }

  def parseGroup(elem: Elem): AlmValidation[(String, Vector[(String, String)])] = {
    for {
      name ← elem \@! "name"
      keys ← parseKeys(elem \\? "key")
    } yield (name, keys)
  }

  def parseKeys(elems: Seq[Elem]): AlmValidation[Vector[(String, String)]] = {
    elems.map { elem ⇒ parseKey(elem).toAgg }.toVector.sequence
  }

  def parseKey(elem: Elem): AlmValidation[(String, String)] = {
    for {
      name ← elem \@! "name"
      value ← elem.text.success
    } yield (name, value)
  }

}