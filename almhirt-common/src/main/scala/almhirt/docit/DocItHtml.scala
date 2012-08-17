package almhirt.docit

import scala.xml._
import scalaz._, Scalaz._
import almhirt.validation._
import almhirt.validation.AlmValidation
import almhirt.validation.Problem._


case class DocItHtmlSettingss(css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty)

class DocItHtml private(treeLoc: TreeLoc[DocItPathPartElement], settings: DocItHtmlSettingss) {
  def getPage(path: List[String]): Validation[NotFoundProblem, Elem] = {
    DocIt.findByPath(treeLoc, path)
      .map(DocItHtml.renderPage(_, settings).success)
      .getOrElse(NotFoundProblem("No documentation found for '%s'".format(path.mkString("/")), Minor).failure)
  }
}

object DocItHtml {
  def apply(tree: TreeLoc[DocItPathPartElement], settings: DocItHtmlSettingss): DocItHtml = 
    new DocItHtml(tree, settings)
    
  def apply(aDocTree: DocTreeNode, settings: DocItHtmlSettingss): DocItHtml = 
    apply(DocIt(aDocTree).loc, settings)
    
  def renderPage(tree: TreeLoc[DocItPathPartElement], settings: DocItHtmlSettingss): Elem = {
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{tree.getLabel.title}</title>
        {settings.css.map(css => <link rel="stylesheet" type="text/css" href={css} />).getOrElse(NodeSeq.Empty)}
      </head>
      <body>
        { settings.styleClassMap.get("title")
            .map(style => <h1 class={style}>{tree.getLabel.title}</h1>)
            .getOrElse (<h1>{tree.getLabel.title}</h1>) }
      </body>
    </html>
  }
}