package almhirt.docit

import scalaz._, Scalaz._

case class DocItHtmlSettingss(css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty)

class DocItHtml private(tree: Tree[PathPartElement], settings: DocItHtmlSettingss)

object DocItHtml {
//  def apply(service: DocTree, css: Option[String] = None, styleClassMap: Map[String, String] = Map.empty): DocItHtml =
//    val tree =
//      
//    
//    new DocItHtml(service, DocItHtmlSettingss(css, styleClassMap))
}