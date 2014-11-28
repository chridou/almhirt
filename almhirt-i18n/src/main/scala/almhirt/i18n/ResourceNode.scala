package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scala.xml._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait DirectResourceLookup {
  def locale: ULocale
  def get(key: ResourceKey): AlmValidation[String]
  final def find(key: ResourceKey): Option[String] = get(key).toOption
}

trait ResourceNode extends DirectResourceLookup {
  def parent: Option[ResourceNode]

  override def get(key: ResourceKey): AlmValidation[String] =
    getLocally(key).fold(
      fail ⇒ {
        fail match {
          case ResourceNotFoundProblem(_) ⇒
            parent match {
              case Some(p) ⇒
                p.get(key)
              case None ⇒
                fail.failure
            }
          case _ ⇒
            fail.failure
        }
      },
      succ ⇒ succ.success)

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
        val locale = theLocale
        val parent = theParent
        def getLocally(key: ResourceKey): AlmValidation[String] =
          keysMap get key match {
            case Some(v) ⇒ v.success
            case None    ⇒ ResourceNotFoundProblem(s"No resource for key $key.").failure
          }
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