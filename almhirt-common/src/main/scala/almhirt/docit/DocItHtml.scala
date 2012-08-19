package almhirt.docit

import scala.xml._
import scalaz._, Scalaz._
import almhirt.validation._
import almhirt.validation.AlmValidation
import almhirt.validation.Problem._


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
    
//  private def accumulatePathElements(pathFromRoot: List[TreeLoc[DocItPathNode]], acc: List[(List[String], DocItPathNode)]): List[(List[String], DocItPathNode)] = {
//    pathFromRoot match {
//      case Nil => 
//        acc
//      case h :: t =>
//        pathFromRoot(t, acc)
//    }
//  }
    
  def renderPage(treeLoc: TreeLoc[DocItPathNode], settings: DocItHtmlSettingss): Elem = {
    val docItem = treeLoc.getLabel
    val pathFromRoot = treeLoc.path.toList.reverse
    val parameters = 
      pathFromRoot.flatMap{case res: ResourceDoc => res.parameter.map((res.name, _)) case _ => None}.toList
    val methods = 
      pathFromRoot.flatMap{case res: ResourceDoc => res.methods case _ => Nil}
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
              <table>{
                methods.map{ method => {
                  settings.styleClassMap.get("methods-row")
                    .map(style => 
                      <tr class={style}>
                        <td>{method.name}</td>
                        <td>
                          <p>{method.description}</p>
                          <
                        </td>
                      </tr>)
                    .getOrElse (<tr><td>{name}</td><td>{desc}</td></tr>)}}}
              </table>
            NodeSeq.fromSeq(Seq(nodeHeader, tableNode))
          }
        }
      </body>
    </html>
  }
}