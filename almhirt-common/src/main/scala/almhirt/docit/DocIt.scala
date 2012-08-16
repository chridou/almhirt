package almhirt.docit

import scala.xml.Node
import scalaz._, Scalaz._

case class DocItHtmlSettingss(css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty)

object DocIt {
  def apply(aDocTree: DocTree): Tree[PathPartElement] = {
    def nodeFromDocTree(docTree: DocTree): Tree[PathPartElement] =
      if(docTree.children.isEmpty)
        docTree.payload.leaf
      else
        docTree.payload.node(docTree.children.map(nodeFromDocTree(_)): _*)      
    nodeFromDocTree(aDocTree)
  }
}