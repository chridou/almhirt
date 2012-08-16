package almhirt.docit

import scalaz._, Scalaz._

case class DocItHtmlSettingss(css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty)

class DocItHtml private(tree: Tree[PathPartElement], settings: DocItHtmlSettingss) {
  
}

object DocItHtml {
  def apply(aDocTree: DocTree, css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty): DocItHtml = {
    def nodeFromDocTree(docTree: DocTree): Tree[PathPartElement] =
      if(docTree.children.isEmpty)
        docTree.payload.leaf
      else
        docTree.payload.node(docTree.children.map(nodeFromDocTree(_)): _*)      
      
    
    new DocItHtml(nodeFromDocTree(aDocTree), DocItHtmlSettingss(css, styleClassMap))
  }
}