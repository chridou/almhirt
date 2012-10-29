/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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