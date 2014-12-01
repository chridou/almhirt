package almhirt.i18n

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.util.ULocale

trait ResourceLookup {
  def resourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, String)]
  def supportedLocales: Set[ULocale]

  def resource(key: ResourceKey, locale: ULocale): AlmValidation[String] = resourceWithLocale(key, locale).map(_._2)
  final def findResource(key: ResourceKey, locale: ULocale): Option[String] = resource(key, locale).toOption
  final def findResourceWithLocale(key: ResourceKey, locale: ULocale): Option[(ULocale, String)] = resourceWithLocale(key, locale).toOption
}

trait AlmResources extends ResourceLookup {
  def withFallback(fallback: AlmResources): AlmValidation[AlmResources]

  def resourceNode(locale: ULocale): AlmValidation[ResourceNode]
  final def findResourceNode(locale: ULocale): Option[ResourceNode] = resourceNode(locale).toOption

  def localesTree: Tree[ULocale]
}

object AlmResources {
  def fromXmlInResources(resourcePath: String, namePrefix: String, classloader: ClassLoader, allowFallback: Boolean = true): AlmValidation[AlmResources] = {
    for {
      factories ← AlmResourcesXml.getFactories(resourcePath, namePrefix, classloader)
      factoriesTree ← TreeBuilder.build(factories)
      tree ← TreeBuilder.executeFactoriesTree(None, factoriesTree)
    } yield fromNodeTree(tree, allowFallback)
  }

  private def getWithFallback(locale: ULocale, from: Map[ULocale, ResourceNode], useFallback: Boolean): AlmValidation[ResourceNode] =
    from get locale match {
      case Some(n) ⇒ n.success
      case None ⇒
        if (useFallback)
          getWithFallback(locale.getFallback, from, false)
        else
          ResourceNotFoundProblem(s""""${locale.getBaseName}" is not a supported locale.""").failure
    }

  def fromNodeTree(tree: Tree[ResourceNode], allowFallback: Boolean): AlmResources = {
    val theLocalesTree = tree.map { _.locale }
    val nodesByLocale = tree.flatten.map(tr ⇒ (tr.locale, tr)).toMap

    new AlmResources {
      def resourceWithLocale(key: ResourceKey, locale: ULocale): AlmValidation[(ULocale, String)] =
        resourceNode(locale).flatMap(node ⇒ node(key).map((node.locale, _)))

      def resourceNode(locale: ULocale): AlmValidation[ResourceNode] =
        getWithFallback(locale, nodesByLocale, allowFallback)

      def supportedLocales: Set[ULocale] = nodesByLocale.keySet
      def localesTree: Tree[ULocale] = theLocalesTree

      def withFallback(fallback: AlmResources): AlmValidation[AlmResources] =
        ???
    }
  }
}

private[almhirt] object TreeBuilder {
  import scalaz.Tree
  import scalaz.Tree._

  def executeFactoriesTree(parent: Option[ResourceNode], tree: Tree[Option[ResourceNode] ⇒ AlmValidation[ResourceNode]]): AlmValidation[Tree[ResourceNode]] = {
    for {
      root ← tree.rootLabel(parent)
      subForest ← tree.subForest.map(child ⇒ executeFactoriesTree(Some(root), child).toAgg).toVector.sequence
    } yield root.node(subForest: _*)
  }

  def build[T](items: Seq[(ULocale, Boolean, T)]): AlmValidation[Tree[T]] = {
    for {
      _ ← {
        val roots = items.filter(_._2 == true)
        roots.size match {
          case 0 ⇒ ArgumentProblem(s"There must be at least one root.").failure
          case 1 ⇒ ().success
          case x ⇒ ArgumentProblem(s"There must be exactly be 1 root. There are $x: ${roots.map(_._1.getBaseName).mkString(", ")}").failure
        }
      }
      _ ← if (items.map(_._1.getBaseName).toSet.size != items.length) {
        ArgumentProblem(s"There is at least one duplicate locale.").failure
      } else {
        ().success
      }
      nodeTree ← {
        val root = items.find(_._2).get
        val rest = items.filterNot(_._2).map(x ⇒ (x._1, x._3))
        buildTree((root._1, root._3), rest)
      }
    } yield nodeTree
  }

  def buildTree[T](root: (ULocale, T), forest: Seq[(ULocale, T)]): AlmValidation[Tree[T]] = {
    if (root._1.getLanguage != root._1.getBaseName) {
      ArgumentProblem(s"The root must be a pure language. It is ${root._1.getBaseName}").failure
    } else {
      for {
        aForest ← makeForests(forest)
      } yield root._2.node(aForest: _*)
    }
  }

  def makeForests[T](nodes: Seq[(ULocale, T)]): AlmValidation[Vector[Tree[T]]] = {
    val explodedLocales = nodes.map({ case (loc, factory) ⇒ (loc.language, loc.script, loc.country, factory) })
    val byLang = explodedLocales.collect({ case (Some(lang), script, terr, fac) ⇒ (lang, (script, terr, fac)) }).groupBy(_._1).values.map(_.map(_._2))
    byLang.map(forLang ⇒ makeForestForLanguage(forLang).toAgg).toVector.sequence.map(_.flatten)
  }

  def makeForestForLanguage[T](forest: Seq[(Option[String], Option[String], T)]): AlmValidation[Vector[Tree[T]]] = {
    val languageOnly = forest.collect({ case (None, None, item) ⇒ item }).toVector
    val withTerrOnly = forest.collect({ case (None, Some(terr), fac) ⇒ fac.leaf })
    val withScript = forest.collect({ case (Some(script), terr, fac) ⇒ (script, terr, fac) })
    if (languageOnly.size > 1) {
      ArgumentProblem(s"There my only be 1 item that only has a language.").failure
    } else if (languageOnly.size == 1) {
      makeScriptsAndTerritoriesTrees(withScript).map(children ⇒ Vector(languageOnly.head.node(children ++ withTerrOnly: _*)))
    } else {
      makeScriptsAndTerritoriesTrees(withScript).map(_ ++ withTerrOnly)
    }
  }

  /**
   * Build the subtrees for a all scripts
   */
  def makeScriptsAndTerritoriesTrees[T](items: Seq[(String, Option[String], T)]): AlmValidation[Vector[Tree[T]]] = {
    val byScripts = items.groupBy(_._1).values.map(_.map(x ⇒ (x._2, x._3)))
    byScripts.map { byScript ⇒ makeScriptsAndTerritoriesTree(byScript).toAgg.sequence }.flatten.toVector.sequence
  }

  /**
   * Build the subtrees for a single script
   */
  def makeScriptsAndTerritoriesTree[T](items: Seq[(Option[String], T)]): AlmValidation[Vector[Tree[T]]] = {
    val itemsWithTerritory = items.collect({ case (Some(terr), item) ⇒ item }).map { _.leaf }.toVector
    val itemsWithoutTerritory = items.collect({ case (None, item) ⇒ item })
    if (itemsWithoutTerritory.size > 1) {
      ArgumentProblem(s"There my only be 1 item without a territory.").failure
    } else {
      itemsWithoutTerritory.headOption match {
        case Some(scriptRoot) ⇒
          Vector(scriptRoot.node(itemsWithTerritory: _*)).success
        case None ⇒
          itemsWithTerritory.success
      }
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