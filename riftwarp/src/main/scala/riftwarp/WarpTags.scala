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

  sealed trait Html
  def HtmlString(v: String): String @@ Html = Tag[String, Html](v)
  
  sealed trait Bson
  def BsonBinary(v: Array[Byte]): Array[Byte] @@ Bson = Tag[Array[Byte], Bson](v)

  sealed trait MessagePack
  def MessagePack(v: Array[Byte]): Array[Byte] @@ MessagePack = Tag[Array[Byte], MessagePack](v)
  
  sealed trait Text
  def TextString(v: String): String @@ Text = Tag[String, Text](v)
  def TextCord(v: Cord): Cord @@ Text = Tag[Cord, Text](v)
  
  sealed trait Exploded
  def ExplodedAny(v: Any): Any @@ Exploded = Tag[Any, Exploded](v)
}