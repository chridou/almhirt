package almhirt.docit

import scalaz._, Scalaz._

trait DocItPathNode{
  def name: String
  def title: String
  def description: String
  def uriPatternPart: String
}

case class DocTreeNode(payload: DocItPathNode, children: List[DocTreeNode] = Nil)

case class ServiceDoc(
  name: String,
  title: String,
  description: String) extends DocItPathNode {
  def uriPatternPart = name
}
  
case class ResourceDoc(
  name: String,
  title: String,
  description: String,
  parameter: Option[String],
  requiresAuthentication: Boolean,
  methods: List[MethodDoc]= Nil) extends DocItPathNode {
  val uriPatternPart = if(parameter.isEmpty) name else "{%s}".format(name)
}
  
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


sealed trait ContentTypeDoc{
  def headerString: String
  def docLink: Option[String]
}

case object AppXml extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/application/index.html""")
  val headerString = "application/xml"
}
case object AppJson extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/application/index.html""")
  val headerString = "application/json"
}
case object ImgJpeg extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/image/index.html""")
  val headerString = "image/jpeg"
}
case object ImgPng extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/image/index.html""")
  val headerString = "image/png"
}

case object TextPlain extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/image/index.html""")
  val headerString = "text/plain"
}

case class CustomContentType(headerString: String, docLink: Option[String] = None) extends ContentTypeDoc

sealed trait MethodParamDoc{
  def name: String
  def description: String
  def isFlag: Boolean
}

case class MethodParamFlag(name: String, description: String) extends MethodParamDoc {
  val isFlag = true
}

case class MethodParamValues(name: String, description: String) extends MethodParamDoc {
  val isFlag = false
}

sealed trait HeaderDoc

