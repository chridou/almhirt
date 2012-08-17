package almhirt.docit

import scala.xml.Node
import scalaz._, Scalaz._

object DocIt {
  def apply(aDocTree: DocTreeNode): Tree[DocItPathNode] = {
    def nodeFromDocTree(docTree: DocTreeNode): Tree[DocItPathNode] =
      if(docTree.children.isEmpty)
        docTree.payload.leaf
      else {
        val subForest = docTree.children.map(nodeFromDocTree(_))
        docTree.payload.node(subForest: _*)     
      }
    nodeFromDocTree(aDocTree)
  }
  
  def findByPath(treeLoc: TreeLoc[DocItPathNode], path: List[String]): Option[TreeLoc[DocItPathNode]] = {
    path match {
      case Nil => 
        Some(treeLoc)
      case h :: t =>
        treeLoc.findChild(child => child.rootLabel.name == h).flatMap(findByPath(_, t))
    }
  }
}