package almhirt.i18n

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.util.ULocale

/**
 * Access resources
 */
trait AlmResources extends ResourceLookup {
  /**
   * @return a tree of the [[PinnedResources]]s hierarchy
   */
  def pinnedResourcesTree: Tree[PinnedResources]

  /**
   * @return a tree of that represents the structure of the [[PinnedResources]]s
   */
  override def localeTree: Tree[ULocale] = pinnedResourcesTree.map { _.locale }

  /** Path from the locale to the root */
  def rootLocalePath(locale: ULocale): AlmValidation[Seq[ULocale]]

  /**
   * Get a [[PinnedResources]] without using a fallback locale
   *
   * @param locale the exact locale for the queried [[PinnedResources]]
   * @return the possibly found [[PinnedResources]]
   */
  def getPinnedResourcesStrict(locale: ULocale): AlmValidation[PinnedResources]

  /**
   * Get a [[PinnedResources]] possibly using a fallback locale or using the root locale
   *
   * @param locale the locale for the queried [[PinnedResources]]
   * @return the possibly found [[PinnedResources]]
   */
  final def getPinnedResources(locale: ULocale): AlmValidation[PinnedResources] = {
    getPinnedResourcesStrict(locale).fold(
      fail ⇒ {
        if (allowsLocaleFallback)
          getPinnedResourcesStrict(locale.getFallback).fold(
            fail ⇒
              if (fallsBackToRoot)
                pinnedResourcesTree.rootLabel.success
              else
                NotFoundProblem(s""""${locale.getBaseName}" is not a supported locale. Even though I used the fallback locale ${locale.getFallback.toLanguageTag()}""").failure,
            succ ⇒ succ.success)
        else
          NotFoundProblem(s""""${locale.getBaseName}" is not a supported locale.""").failure
      },
      succ ⇒ succ.success)
  }

  /**
   * Adds resources (and languages) not in this [[almhirt.i18n.AlmResources]].
   *
   * The root locale will stay the same.
   *
   * Does not overwrite already existing resources.
   *
   * @param fallback the resources to use as a fallback
   * @return the new resources
   */
  def withFallback(fallback: AlmResources, fallbackToNewLanguages: Boolean): AlmValidation[AlmResources]

  override def selectOneFrom[L](locale: L, from: Set[ULocale])(implicit magnet: LocaleMagnet[L]): Option[ULocale] = {
    @scala.annotation.tailrec
    def tryFindUpwards(innerFrom: Set[ULocale], rest: List[ULocale]): Option[ULocale] = {
      rest match {
        case Nil => None
        case x :: xs =>
          if(from.contains(x)) {
            Some(x)
          } else if(this.doesUpwardLookup){
            tryFindUpwards(innerFrom, xs)
          } else {
            None
          }
      }
    }
    
    val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
    rootLocalePath(uLoc).fold (
      fail => None,
      path => {
        val pathList = path.toList
        tryFindUpwards(from, pathList)
      }
    )
  }

}

object AlmResources {
  def fromXmlInResources(resourcePath: String, namePrefix: String, classloader: ClassLoader, allowUpwardsLookup: Boolean, allowFallback: Boolean, fallBackToRootAllowed: Boolean): AlmValidation[AlmResources] = {
    for {
      nodes ← AlmResourcesXml.getNodes(resourcePath, namePrefix, classloader)
      nodeTree ← TreeBuilder.build(nodes)
    } yield TreeBuilder.fromNodeTree(nodeTree, allowUpwardsLookup, allowFallback, fallBackToRootAllowed)
  }

  def fromXml(pinnedResources: Seq[scala.xml.Elem], allowUpwardsLookup: Boolean, allowFallback: Boolean, fallBackToRootAllowed: Boolean): AlmValidation[AlmResources] = {
    for {
      prepared ← AlmResourcesXml.getLocalesAndIsRoot(pinnedResources)
      nodes ← prepared.map({ case (elem, locale, isRoot) ⇒ PinnedResources.fromXml(elem).map((locale, isRoot, _)).toAgg }).sequence
      nodeTree ← TreeBuilder.build(nodes)
    } yield TreeBuilder.fromNodeTree(nodeTree, allowUpwardsLookup, allowFallback, fallBackToRootAllowed)
  }

  val empty = new AlmResources {
    override val allowsLocaleFallback = false
    override val doesUpwardLookup = false
    override val fallsBackToRoot = false
    override val supportedLocales = Set.empty[ULocale]
    override def pinnedResourcesTree = throw new NoSuchElementException("There is no node tree in the empty resources!")
    override def localeTree = throw new NoSuchElementException("There is no locale tree in the empty resources!")
    override def rootLocalePath(locale: ULocale): AlmValidation[Seq[ULocale]] = ArgumentProblem("The empty AlmResources does not contain any locales").failure
    override def getPinnedResourcesStrict(locale: ULocale): AlmValidation[PinnedResources] = ArgumentProblem("The empty AlmResources does not contain any nodes").failure
    override def getResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, ResourceValue)] = NotFoundProblem("The empty AlmResources does not contain any resources").failure
    override def withFallback(fallback: AlmResources, fallbackToNewLanguages: Boolean): AlmValidation[AlmResources] = fallback.success
  }

  implicit class SuperMegaOps(val resources: AlmResources) extends AnyVal {
    def pinAllAccessibleResources(locale: ULocale): AlmValidation[PinnedResources] = {
      for {
        pinnedRes ← resources.getPinnedResources(locale)
        pathToRoot ← resources.rootLocalePath(pinnedRes.locale)
        allMappingsFromRoot ← pathToRoot.toList.map(loc ⇒ resources.getPinnedResources(loc).map(_.mappings).toAgg).sequence.map(_.reverse)
      } yield {
        val mergedMappings = allMappingsFromRoot.tail.foldLeft(allMappingsFromRoot.head) { (acc, cur) ⇒
          acc ++ cur
        }
        PinnedResources(locale, mergedMappings)
      }
    }
    def pinAllAccessibleResourcesInSection(section: ResourceSection, locale: ULocale): AlmValidation[PinnedResources] =
      pinAllAccessibleResources(locale).map(res ⇒ PinnedResources(locale, res.mappings.filterKeys { _.section == section.section }))

    def pinAllAccessibleResourcesInGroup(group: ResourceGroup, locale: ULocale): AlmValidation[PinnedResources] =
      pinAllAccessibleResources(locale).map(res ⇒ PinnedResources(locale, res.mappings.filterKeys { key ⇒ key.section == group.section && key.group == group.group }))
  }
}

private[almhirt] object TreeBuilder {
  def makeTreeWithParents(tree: Tree[PinnedResources], parent: Option[PinnedResources]): Seq[(PinnedResources, Option[PinnedResources])] = {
    Seq((tree.rootLabel, parent)) ++ tree.subForest.toList.map { child ⇒ makeTreeWithParents(child, Some(tree.rootLabel)) }.flatten
  }

  private def find(key: ResourceKey, current: PinnedResources, resourcesWithParents: Map[ULocale, Option[PinnedResources]], doUpwardLookup: Boolean): Option[(ULocale, ResourceValue)] = {
    current(key) match {
      case scalaz.Success(resource) ⇒ Some(current.locale, resource)
      case scalaz.Failure(_) ⇒
        if (doUpwardLookup)
          resourcesWithParents(current.locale) match {
            case Some(parent) ⇒ find(key, parent, resourcesWithParents, doUpwardLookup)
            case None         ⇒ None
          }
        else
          None
    }
  }

  private def pathToRoot(locale: Option[ULocale], resourcesWithParents: Map[ULocale, Option[ULocale]]): List[ULocale] = {
    locale match {
      case None    ⇒ Nil
      case Some(l) ⇒ l :: pathToRoot(resourcesWithParents.get(l).flatten, resourcesWithParents)
    }
  }

  def fromNodeTree(tree: Tree[PinnedResources], allowUpwardsLookup: Boolean, allowFallback: Boolean, fallBackToRootAllowed: Boolean): AlmResources = {
    val theLocalesTree = tree.map { _.locale }
    val nodesByLocale = tree.flatten.map(tr ⇒ (tr.locale, tr)).toMap

    val resourcesWithParents = makeTreeWithParents(tree, None).map { case (resources, parent) ⇒ (resources.locale, parent) }.toMap

    new AlmResources {
      val allowsLocaleFallback = allowFallback
      val doesUpwardLookup = allowUpwardsLookup
      val fallsBackToRoot = fallBackToRootAllowed
      override def getPinnedResourcesStrict(locale: ULocale): AlmValidation[PinnedResources] =
        nodesByLocale get (locale) match {
          case Some(node) ⇒ node.success
          case None       ⇒ NotFoundProblem(s""""${locale.getBaseName}" is not a supported locale.""").failure
        }

      override val supportedLocales: Set[ULocale] = nodesByLocale.keySet

      override val pinnedResourcesTree: Tree[PinnedResources] = tree
      override val localeTree: Tree[ULocale] = theLocalesTree

      override def rootLocalePath(locale: ULocale): AlmValidation[Seq[ULocale]] = {
        for {
          entryLocale ← getPinnedResources(locale).map(_.locale)
        } yield pathToRoot(Some(entryLocale), resourcesWithParents.mapValues(_.map(_.locale)))
      }

      override def getResourceWithLocale[L: LocaleMagnet](key: ResourceKey, locale: L): AlmValidation[(ULocale, ResourceValue)] = {
        val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
        this.getPinnedResources(uLoc) fold (
          fail ⇒ ResourceNotFoundProblem(s""""${uLoc.getBaseName}" is not a supported locale. The queried key is $key.""", cause = Some(fail)).failure,
          startWith ⇒
            find(key, startWith, resourcesWithParents, allowUpwardsLookup) match {
              case Some(res) ⇒ res.success
              case None      ⇒ ResourceNotFoundProblem(s"""$key not found(even after an upwards lookup) for locale ${uLoc.getBaseName}".""").failure
            })
      }

      override def withFallback(fallback: AlmResources, fallbackToNewLanguages: Boolean): AlmValidation[AlmResources] =
        addFallback(this, fallback, fallbackToNewLanguages)

    }
  }

  private def addFallback(orig: AlmResources, fallback: AlmResources, fallbackToNewLanguages: Boolean): AlmValidation[AlmResources] = {
    if (fallback.supportedLocales.isEmpty) {
      orig.success
    } else {
      val origNodes = orig.pinnedResourcesTree.flatten.map(t ⇒ (t.locale, t)).toMap
      val fallbackNodes = fallback.pinnedResourcesTree.flatten.map(t ⇒ (t.locale, t)).toMap

      val mergedV =
        fallbackNodes.foldLeft(origNodes.success[almhirt.common.Problem]) {
          case (accV, (nextLoc, nextNode)) ⇒
            accV match {
              case scalaz.Failure(_) ⇒
                accV
              case scalaz.Success(acc) ⇒
                acc get nextLoc match {
                  case Some(toAddFallbacksTo) ⇒
                    toAddFallbacksTo.withFallback(nextNode).map(merged ⇒ acc + (nextLoc -> merged))
                  case None ⇒
                    if (fallbackToNewLanguages)
                      (acc + (nextLoc -> nextNode)).success
                    else
                      acc.success
                }
            }
        }

      val treeV = mergedV.flatMap({ merged ⇒
        val rootLocale = orig.localeTree.rootLabel
        val root = (rootLocale, merged(rootLocale))
        val rest = merged - rootLocale
        buildTree[PinnedResources](root, rest.toSeq)
      })

      treeV.map(tree ⇒ fromNodeTree(tree, orig.doesUpwardLookup, orig.allowsLocaleFallback, orig.fallsBackToRoot))
    }
  }

  def build(items: Seq[(ULocale, Boolean, PinnedResources)]): AlmValidation[Tree[PinnedResources]] = {
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

  def getNodes(resourcePath: String, namePrefix: String, classloader: ClassLoader): AlmValidation[Seq[(ULocale, Boolean, PinnedResources)]] = {
    for {
      files ← AlmClassLoaderResourcesHelper.getFilesToLoad(resourcePath, namePrefix, classloader).flatMap(fs ⇒
        if (fs.isEmpty)
          MandatoryDataProblem("No files to load!").failure
        else
          fs.success)
      xmls ← inTryCatch {
        files.map { fn ⇒
          val stream = classloader.getResourceAsStream(fn)
          if (stream != null)
            try {
              val xml = IOUtils.toString(stream, Charsets.UTF_8)
              XML.loadString(xml)
            } finally {
              stream.close
            }
          else {
            throw new Exception(s"No resources found for $fn.")
          }
        }
      }
      prepared ← getLocalesAndIsRoot(xmls)
      nodes ← prepared.map({ case (elem, locale, isRoot) ⇒ PinnedResources.fromXml(elem).map((locale, isRoot, _)).toAgg }).sequence
    } yield nodes
  }

  def getLocalesAndIsRoot(xmls: Seq[Elem]): AlmValidation[List[(Elem, ULocale, Boolean)]] = {
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
    resV.toList.sequence
  }

}