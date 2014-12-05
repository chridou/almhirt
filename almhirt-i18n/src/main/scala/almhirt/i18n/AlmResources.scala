package almhirt.i18n

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.util.ULocale

trait AlmResources extends ResourceLookup {
  def withFallback(fallback: AlmResources): AlmValidation[AlmResources]
}

object AlmResources {
  def fromXmlInResources(resourcePath: String, namePrefix: String, classloader: ClassLoader, allowFallback: Boolean = true): AlmValidation[AlmResources] = {
    for {
      factories ← AlmResourcesXml.getFactories(resourcePath, namePrefix, classloader)
      factoriesTree ← TreeBuilder.build(factories)
      tree ← TreeBuilder.executeFactoriesTree(None, factoriesTree)
    } yield TreeBuilder.fromNodeTree(tree, allowFallback)
  }

  val empty = new AlmResources {
    val allowsLocaleFallback = false
    val supportedLocales = Set.empty[ULocale]
    def localesTree = throw new NoSuchElementException("There is no locales tree in the empty resources!")
    def resourceNodeStrict(locale: ULocale): AlmValidation[ResourceNode] = ArgumentProblem("The empty AlmResources does not contain any nodes").failure
    def withFallback(fallback: AlmResources): AlmValidation[AlmResources] = ???
  }
}

private[almhirt] object TreeBuilder {
  def fromNodeTree(tree: Tree[ResourceNode], allowFallback: Boolean): AlmResources = {
    val theLocalesTree = tree.map { _.locale }
    val nodesByLocale = tree.flatten.map(tr ⇒ (tr.locale, tr)).toMap

    new AlmResources {
      val allowsLocaleFallback = allowFallback
      def resourceNodeStrict(locale: ULocale): AlmValidation[ResourceNode] =
        nodesByLocale get (locale) match {
          case Some(node) ⇒ node.success
          case None       ⇒ ResourceNotFoundProblem(s""""${locale.getBaseName}" is not a supported locale.""").failure
        }

      val supportedLocales: Set[ULocale] = nodesByLocale.keySet

      def localesTree: Tree[ULocale] = theLocalesTree

      def withFallback(fallback: AlmResources): AlmValidation[AlmResources] =
        ???
    }
  }

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

private[almhirt] object AlmClassLoaderResourcesHelper {
  import org.apache.commons.io.{ IOUtils, Charsets }
  import scala.collection.JavaConversions._
  import scala.util.matching.Regex
  def getFilesToLoad(resourcePath: String, namePrefix: String, classloader: ClassLoader): AlmValidation[Seq[String]] =
    inTryCatch {
      val filesToLoad = IOUtils.readLines(classloader.getResourceAsStream(s"$resourcePath/$namePrefix.txt"), Charsets.UTF_8)
      if (filesToLoad == null) {
        throw new Exception(s""""$resourcePath/$namePrefix.txt" not found.""")
      } else {
        filesToLoad.toSeq.map { _.trim() }.filterNot(_.isEmpty()).map(loc ⇒ s"$resourcePath/${namePrefix}_$loc.xml")
      }
    }.leftMap(p ⇒ UnspecifiedProblem(s"""Could not determine the files to load.""", cause = Some(p)))
}

private[almhirt] object AlmResourcesXml {
  import org.apache.commons.io.{ IOUtils, Charsets }
  import almhirt.almvalidation.kit._
  import almhirt.xml.all._
  import scala.xml._

  def getFactories(resourcePath: String, namePrefix: String, classloader: ClassLoader): AlmValidation[Seq[(ULocale, Boolean, Option[ResourceNode] ⇒ AlmValidation[ResourceNode])]] = {
    for {
      files ← AlmClassLoaderResourcesHelper.getFilesToLoad(resourcePath, namePrefix, classloader).flatMap(fs ⇒
        if (fs.isEmpty)
          MandatoryDataProblem("No files to load!").failure
        else
          fs.success)
      xmls ← inTryCatch {
        files.map { fn ⇒
          val stream = classloader.getResourceAsStream(fn)
          try {
            val xml = IOUtils.toString(stream, Charsets.UTF_8)
            XML.loadString(xml)
          } finally {
            stream.close
          }
        }
      }
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