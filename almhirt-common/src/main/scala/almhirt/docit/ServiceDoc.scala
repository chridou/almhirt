package almhirt.docit

import scalaz._, Scalaz._

trait PathPartElement{
  def name: String
  def title: String
}

case class DocTree(payload: PathPartElement, children: List[DocTree] = Nil)

case class ServiceDoc(
  name: String,
  title: String,
  description: String) extends PathPartElement
  
case class ResourceDoc(
  name: String,
  title: String,
  description: String,
  parameter: Option[String],
  requiresAuthentication: Boolean,
  methods: List[MethodDoc]= Nil) extends PathPartElement
  
sealed trait MethodDoc {
  def description: String
  def methodParams: List[MethodParamDoc]
  def contentTypes: List[ContentTypeDoc]
  def headers: List[HeaderDoc]
}

case class GET(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc
case class PUT(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc
case class POST(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc
case class DELETE(description: String, methodParams: List[MethodParamDoc] = Nil, contentTypes: List[ContentTypeDoc] = Nil, headers: List[HeaderDoc] = Nil) extends MethodDoc

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
