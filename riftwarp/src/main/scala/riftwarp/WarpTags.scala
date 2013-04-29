package riftwarp

import scalaz._, Scalaz._

object WarpTags {
  sealed trait Json
  def JsonString(v: String): String @@ Json = Tag[String, Json](v)
  def JsonCord(v: Cord): Cord @@ Json = Tag[Cord, Json](v)
  
  sealed trait JsonStdLib
  def JsonStdLib(v: Any): Any @@ JsonStdLib = Tag[Any, JsonStdLib](v)

  sealed trait Xml
  def XmlString(v: String): String @@ Xml = Tag[String, Xml](v)
}