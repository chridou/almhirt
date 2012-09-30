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

import scala.xml._
import scalaz._, Scalaz._
import almhirt._
import almhirt.syntax.almvalidation._


case class DocItHtmlSettingss(css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty)

class DocItHtml private(treeLoc: TreeLoc[DocItPathNode], settings: DocItHtmlSettingss) {
  def getPage(path: List[String]): Validation[NotFoundProblem, Elem] = {
    DocIt.findByPath(treeLoc, path)
      .map(DocItHtml.renderPage(_, settings).success)
      .getOrElse(NotFoundProblem("No documentation found for '%s'".format(path.mkString("/")), Minor).failure)
  }
}

object DocItHtml {
  def apply(tree: TreeLoc[DocItPathNode], settings: DocItHtmlSettingss): DocItHtml = 
    new DocItHtml(tree, settings)
    
  def apply(aDocTree: DocTreeNode, settings: DocItHtmlSettingss): DocItHtml = 
    apply(DocIt(aDocTree).loc, settings)
    
  private def relativeUriToElementsFromPath(path: List[DocItPathNode], acc: List[(List[String], DocItPathNode)]): List[(List[String], DocItPathNode)] = {
    path match {
      case Nil => 
        acc.map{case(path, elem) => (path.reverse, elem)}.reverse
      case h :: t =>
        acc match {
          case Nil =>
            relativeUriToElementsFromPath(t, (List(h.name), h) :: acc)
          case accHead :: _ =>
            relativeUriToElementsFromPath(t, (h.name :: accHead._1, h) :: acc)
        }
    }
  }
  
  private def createBreadcrumbNavigation(pathFromRoot: List[DocItPathNode]): NodeSeq = {
    val parts =
      relativeUriToElementsFromPath(pathFromRoot, Nil)
        .flatMap{case(parts, node) =>
          <a href={"/%s".format(parts.mkString("/"))}>{node.uriPatternPart}</a><span>/</span>}
    NodeSeq.fromSeq(parts)
  }
  
  private def getPathToChild(from: TreeLoc[DocItPathNode], to: DocItPathNode): List[DocItPathNode] = {
    def getPath(from: TreeLoc[DocItPathNode], to: TreeLoc[DocItPathNode], acc: List[DocItPathNode]): List[DocItPathNode] = {
      if(from.getLabel == to.getLabel)
        acc
      else
        getPath(from, to.parent.get, to.getLabel :: acc)
    }
    from.find(tl => tl.getLabel == to)
      .map(getPath(from, _, Nil))
      .getOrElse(Nil)
  }
  
  private def getTreeNavigationFromNode(from: TreeLoc[DocItPathNode]) = {
    val prefix = 
      from.path.reverse.map(_.name).toList match {
        case Nil => "/"
        case x => "/%s/".format(x.mkString("/"))
      }
    val entries =
      from.tree.subForest.flatMap{ subForest =>
        subForest
          .flatten
          .map(to => (to, getPathToChild(from, to)))
          .map{case(to, path) => (to, relativeUriToElementsFromPath(path, Nil))}
          .map{case(to, reachableChildPath) => 
            val link = 
              reachableChildPath.flatMap{case(parts, node) =>
                <a href={"%s%s".format(prefix, parts.mkString("/"))}>{node.uriPatternPart}</a><span>/</span>}
            (to, link)}
          .map{case (to,linkedPath) => <tr><td>{linkedPath}</td><td>{to.title}</td></tr>}}
    
    <table>{entries}</table>
  } 
  
  def renderPage(treeLoc: TreeLoc[DocItPathNode], settings: DocItHtmlSettingss): Elem = {
    val docItem = treeLoc.getLabel
    val pathFromRoot = treeLoc.path.toList.reverse
    val parameters = 
      pathFromRoot.flatMap{case res: ResourceDoc => res.parameter.map((res.name, _)) case _ => None}.toList
    val methods = 
      docItem match {case res: ResourceDoc => res.methods case _ => Nil}
    val requiresAuthentication = docItem match { case res: ResourceDoc => res.requiresAuthentication case _ => false }
    <html>
      <head>
        <title>{docItem.title}</title>
        {settings.css.map(css => <link rel="stylesheet" type="text/css" href={css} />).getOrElse(NodeSeq.Empty)}
      </head>
      <body>
        { settings.styleClassMap.get("title")
            .map(style => <h1 class={style}>{docItem.title}</h1>)
            .getOrElse (<h1>{docItem.title}</h1>) }
        
        { createBreadcrumbNavigation(pathFromRoot) }

        { if(requiresAuthentication)
             settings.styleClassMap.get("requiresAuthentication")
                .map(style => <p class={style}>Requires authentication!</p>)
                    .getOrElse (<p>Requires authentication!</p>)
          else
            NodeSeq.Empty  
        }
        
        { if(!pathFromRoot.isEmpty)
            NodeSeq.Empty
          else
            NodeSeq.Empty  
        }

        { if(!parameters.isEmpty) {
            val nodeHeader =
              settings.styleClassMap.get("parameters-header")
                .map(style => <h2 class={style}>Parameters:</h2>)
                    .getOrElse (<h2>Parameters:</h2>)
            val tableNode =
              <table>{
                parameters.map{ case (name, desc) => {
                  settings.styleClassMap.get("parameters-row")
                    .map(style => <tr class={style}><td>{name}</td><td>{desc}</td></tr>)
                    .getOrElse (<tr><td>{name}</td><td>{desc}</td></tr>)}}}
              </table>
            NodeSeq.fromSeq(Seq(nodeHeader, tableNode))
          }
          else
            NodeSeq.Empty }


        { settings.styleClassMap.get("description-header")
            .map(style => <h2 class={style}>Description:</h2>)
            .getOrElse (<h2>Description:</h2>) }

        { settings.styleClassMap.get("description")
            .map(style => <p class={style}>{docItem.description}</p>)
            .getOrElse (<p>{docItem.description}</p>) }
        
        { if(methods.isEmpty)
            NodeSeq.Empty
          else {
            val nodeHeader =
              settings.styleClassMap.get("methods-header")
                .map(style => <h2 class={style}>Methods:</h2>)
                  .getOrElse (<h2>Methods:</h2>)
              
            val tableNode =
              <table border="1">{
                methods.map{ method => {
                  settings.styleClassMap.get("methods-row")
                    .map(style => 
                      <tr class={style}>
                        <td>{method.name}</td>
                        <td>
                          <h3>Description:</h3>
                          <p>{method.description}</p>
                          <h3>Parameters:</h3>
                          { method.methodParams.map(p => 
                            <div>
                          	   { if(p.isFlag)
                                   <h4>{p.name}(Flag)</h4>
                          	     else
                          	       <h4>{p.name}</h4> }
                              <p>{p.description}</p>
                            </div>)}
                          <h3>ContentTypes:</h3>
                          { method.contentTypes.map(ct => 
                              ct.docLink.map(dl => <p><a href={dl}>{ct.headerString}</a></p>)
                                .getOrElse(<p>{ct.headerString}</p>))}
                        </td>
                      </tr>)
                    .getOrElse {
                      <tr>
                        <td>{method.name}</td>
                        <td>
                          <h3>Description:</h3>
                          <p>{method.description}</p>
                          <h3>Parameters:</h3>
                          { method.methodParams.map(p => 
                            <div>
                          	   { if(p.isFlag)
                                   <h4>{p.name}(Flag)</h4>
                          	     else
                          	       <h4>{p.name}</h4> }
                              <p>{p.description}</p>
                            </div>)}
                          <h3>ContentTypes:</h3>
                            { method.contentTypes.map(ct => 
                              ct.docLink.map(dl => <p><a href={dl}>{ct.headerString}</a></p>)
                                .getOrElse(<p>{ct.headerString}</p>))}
                        </td>
                      </tr>
                }}}}
              </table>
            NodeSeq.fromSeq(Seq(nodeHeader, tableNode))
          }
        }
        
        <h2>Reachable from here:</h2>

        { getTreeNavigationFromNode(treeLoc) }
      </body>
    </html>
  }
}