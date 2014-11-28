package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ResourceLookup {
  def resource(key: ResourceKey, locale: ULocale): AlmValidation[String]
  final def findResource(key: ResourceKey, locale: ULocale): Option[String] = resource(key, locale).toOption
  def supportedLocales: Seq[ULocale]
}

trait AlmResources extends ResourceLookup {
  def withFallback(fallback: AlmResources): AlmResources

  def resourceNode(locale: ULocale): AlmValidation[ResourceNode]
  final def findResourceNode(locale: ULocale): Option[ResourceNode] = resourceNode(locale).toOption
}

object AlmResources {
  def xmlFromResources(resourcePath: String, namePrefix: String, classloader: ClassLoader): AlmValidation[AlmResources] =
    for {
      factories ← AlmResourcesXml.getFactories(resourcePath, namePrefix, classloader)
      resources ← AlmResourcesBuilder.buildFromFactories(factories)
    } yield resources

}

private[almhirt] object AlmResourcesBuilder {
  def buildFromFactories(factories: Seq[(ULocale, Boolean, Option[ResourceNode] ⇒ AlmValidation[ResourceNode])]): AlmValidation[AlmResources] = {
    for {
      _ ← {
        val roots = factories.filter(_._2 == true)
        roots.size match {
          case 0 ⇒ ArgumentProblem(s"There must be at least one root.").failure
          case 1 ⇒ ().success
          case x ⇒ ArgumentProblem(s"There must be exactly be 1 root. There are $x: ${roots.map(_._1.getBaseName).mkString(", ")}").failure
        }
      }
      _ ← if (factories.map(_._1.getBaseName).toSet.size != factories.length) {
        ArgumentProblem(s"There is at least duplicate locale.").failure
      } else {
        ().success
      }
      resources ← {
        val root = factories.find(_._2).get
        val rest = factories.filterNot(_._2).map(x ⇒ (x._1, x._3))
        buildTree((root._1, root._3), rest)
      }
    } yield resources
  }

  def buildTree(root: (ULocale, Option[ResourceNode] ⇒ AlmValidation[ResourceNode]), rest: Seq[(ULocale, Option[ResourceNode] ⇒ AlmValidation[ResourceNode])]): AlmValidation[AlmResources] = {
    if (root._1.getLanguage != root._1.getBaseName) {
      ArgumentProblem(s"The root must be a pure language. It is ${root._1.getBaseName}").failure
    } else {
      ???
    }
  }
}

private[almhirt] object AlmResourcesHelper {
  import java.io._
  import scala.collection.JavaConversions._
  import scala.util.matching.Regex
  def getFilesInResources(resourcePath: String, classloader: ClassLoader): AlmValidation[Seq[File]] =
    inTryCatch {
      val url = classloader.getResource(resourcePath)
      if (url == null) {
        throw new Exception(s"No resources at $resourcePath.")
      } else {
        val dir = new File(url.toURI());
        dir.listFiles().toSeq
      }
    }

  def getFilesInResourcesWithPattern(resourcePath: String, pattern: Regex, classloader: ClassLoader): AlmValidation[Seq[File]] =
    getFilesInResources(resourcePath, classloader).map(_.filter(file ⇒ pattern.findFirstIn(file.getName).isDefined))

}

private[almhirt] object AlmResourcesXml {
  import scalaz._, Scalaz._
  import almhirt.almvalidation.kit._
  import almhirt.xml.all._
  import scala.xml._

  def getFactories(resourcePath: String, namePrefix: String, classloader: ClassLoader): AlmValidation[Seq[(ULocale, Boolean, Option[ResourceNode] ⇒ AlmValidation[ResourceNode])]] = {
    for {
      files ← AlmResourcesHelper.getFilesInResourcesWithPattern(resourcePath, s"$namePrefix.*\\.xml".r, classloader)
      xmls ← inTryCatch { files.map { XML.loadFile(_) } }
      prepared ← getLocalesAndIsRoot(xmls)
    } yield prepared.map({ case (elem, locale, isRoot) ⇒ (locale, isRoot, (parent: Option[ResourceNode]) ⇒ ResourceNode.fromXml(elem, parent)) })
  }

  def getLocalesAndIsRoot(xmls: Seq[Elem]): AlmValidation[Seq[(Elem, ULocale, Boolean)]] = {
    val resV = xmls.map { elem ⇒
      (for {
        isRoot ← elem \@? "root" match {
          case None        ⇒ false.success
          case Some(value) ⇒ value.toBooleanAlm
        }
        localeAttV ← elem \@! "locale"
        locale ← inTryCatch { new ULocale(localeAttV) }
      } yield (elem, locale, isRoot)).toAgg
    }
    resV.toVector.sequence.map(_.toSeq)
  }

}