package almhirt.docit

import scalaz._, Scalaz._

trait DocItPathNode{
  def name: String
  def title: String
  def description: String
}

case class DocTreeNode(payload: DocItPathNode, children: List[DocTreeNode] = Nil)

case class ServiceDoc(
  name: String,
  title: String,
  description: String) extends DocItPathNode
  
case class ResourceDoc(
  name: String,
  title: String,
  description: String,
  parameter: Option[String],
  requiresAuthentication: Boolean,
  methods: List[MethodDoc]= Nil) extends DocItPathNode
  
sealed trait MethodDoc {
  def name: String
  def description: String
  def methodParams: List[MethodParamDoc]
  def contentTypes: List[ContentTypeDoc]
  def headers: List[HeaderDoc]
}

case class GET(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc { val name = "GET"}
case class PUT(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc { val name = "PUT"}
case class POST(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc { val name = "POST"}
case class DELETE(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc { val name = "DELETE"}

sealed trait MethodParamDoc
sealed trait HeaderDoc

sealed trait ContentTypeDoc{
  def headerString: String
  def docLink: Option[String]
}

case class AppXml() extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/application/index.html""")
  val headerString = "application/xml"
}
case class AppJson() extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/application/index.html""")
  val headerString = "application/json"
}
case class ImgJpeg() extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/image/index.html""")
  val headerString = "image/jpeg"
}
case class ImgPng() extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/image/index.html""")
  val headerString = "image/png"
}

case class CustomContentType(headerString: String, docLink: Option[String]) extends ContentTypeDoc
