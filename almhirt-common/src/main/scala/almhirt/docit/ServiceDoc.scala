package almhirt.docit

import scalaz._, Scalaz._


/** Path of a documentation hierarchy for REST services*/
trait DocItPathNode{
  /** The name of this item on the path */
  def name: String
  /** A title for display */
  def title: String
  /** A detailed description */
  def description: String
  /** How is this node to be displayed as part of an URI-Pattern? */
  def uriPatternPart: String
}

/** An unnecessary helper class in case you don't want to build up your hierarchy by scalz's tree */
case class DocTreeNode(payload: DocItPathNode, children: List[DocTreeNode] = Nil)

/** The node is a service or an URI that doesn't represent a resource */
case class ServiceDoc(
  name: String,
  title: String,
  description: String) extends DocItPathNode {
  def uriPatternPart = name
}

/** The node directly represents a resource with the appropriate methods */ 
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
  val docLink = Some("""http://www.iana.org/assignments/media-types/text/index.html""")
  val headerString = "text/plain"
}

case object TextXml extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/text/index.html""")
  val headerString = "text/xml"
}

case object TextRichText extends ContentTypeDoc {
  val docLink = Some("""http://www.iana.org/assignments/media-types/text/index.html""")
  val headerString = "text/richtext"
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

