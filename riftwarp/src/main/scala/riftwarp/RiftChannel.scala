package riftwarp

import riftwarp.components.MemoizesChannels
import riftwarp.http._


trait RiftChannel {
  /** Unique identifier of a channel(like "json", "xml"...)
   * 
   * CONTRACT: MAY NOT BE LONGER THAN 16 CHARS
   */
  def channelType: String
  /** More symbols for lookup  
   * 
   */
  def moreLookUpSymbols: List[String]
  
  def canEqual(other: Any) = {
    other.isInstanceOf[riftwarp.RiftChannel]
  }

  override def equals(other: Any) = {
    other match {
      case that: riftwarp.RiftChannel => that.canEqual(RiftChannel.this) && channelType == that.channelType
      case _ => false
    }
  }

  override def hashCode() = {
    channelType.hashCode
  }

  override def toString() = channelType
  
}

trait RiftHttpChannel extends RiftChannel {
  /** The HTTP content type 
   * 
   */
  def httpContentType: String
  /** The HTTP content type to append when a TypeDescriptor is used to create a content type  
   * 
   */
  def httpContentTypeExt: String
  def httpDimensionType(nice: Boolean): Class[_ <: RiftDimension]
  def httpBodyType: RiftHttpBodyType
}

class RiftText() extends RiftHttpChannel {
  val channelType = "text"
  val httpContentType = "text/plain"
  val httpContentTypeExt = "text"
  val moreLookUpSymbols = Nil
  val httpBodyType = RiftStringBodyType
  def httpDimensionType(nice: Boolean ) = classOf[DimensionString]
}
object RiftText {
  private val theInstance = new RiftText()
  def apply() = theInstance
}

class RiftMap() extends RiftChannel {
  val channelType = "map"
  val httpContentType = None
  val httpContentTypeExt = None
  val moreLookUpSymbols = Nil
}
object RiftMap {
  private val theInstance = new RiftMap()
  def apply() = theInstance
}

class RiftJson() extends RiftHttpChannel {
  val channelType = "json"
  val httpContentType = "text/x-json"
  val httpContentTypeExt = "json"
  val moreLookUpSymbols = Nil
  val httpBodyType = RiftStringBodyType
  def httpDimensionType(nice: Boolean ) = if(nice) classOf[DimensionNiceString] else classOf[DimensionString]
}
object RiftJson {
  private val theInstance = new RiftJson()
  def apply() = theInstance
}

class RiftBson() extends RiftChannel {
  val channelType = "bson"
  val httpContentType = None
  val httpContentTypeExt = None
  val moreLookUpSymbols = Nil
}
object RiftBson {
  private val theInstance = new RiftBson()
  def apply() = theInstance
}

class RiftXml() extends RiftHttpChannel {
  val channelType = "xml"
  val httpContentType = "text/xml"
  val httpContentTypeExt = "xml"
  val moreLookUpSymbols = Nil
  val httpBodyType = RiftStringBodyType
  def httpDimensionType(nice: Boolean ) = 
    if(nice) classOf[DimensionNiceString] else classOf[DimensionString]
}
object RiftXml {
  private val theInstance = new RiftXml()
  def apply() = theInstance
}

class RiftMessagePack() extends RiftHttpChannel {
  val channelType = "msgpack"
  val httpContentType = "x-pack"
  val httpContentTypeExt = "pack"
  val moreLookUpSymbols = Nil
  val httpBodyType = RiftStringBodyType
  def httpDimensionType(nice: Boolean) = classOf[DimensionString]
}
object RiftMessagePack {
  private val theInstance = new RiftMessagePack()
  def apply() = theInstance
}

class RiftProtocolBuffers() extends RiftChannel {
  val channelType = "protobuf"
  val httpContentType = None
  val httpContentTypeExt = None
  val moreLookUpSymbols = Nil
  def preferredDimensionType(nice: Boolean ) = None
}
object RiftProtocolBuffers {
  private val theInstance = new RiftProtocolBuffers()
  def apply() = theInstance
}

//class RiftThrift() extends RiftChannel {
//  val channelType = "thrift"
//  val httpContentType = None
//  val httpContentTypeExt = None
//  val moreLookUpSymbols = Nil
//}
//object RiftThrift {
//  private val theInstance = new RiftThrift()
//  def apply() = theInstance
//}
//
//class RiftYaml() extends RiftChannel {
//  val channelType = "yaml"
//  val httpContentType = None
//  val httpContentTypeExt = None
//  val moreLookUpSymbols = Nil
//}
//object RiftYaml {
//  private val theInstance = new RiftYaml()
//  def apply() = theInstance
//}

object RiftChannel {
  val Text = RiftText()
  val Map = RiftMap()
  val Json = RiftJson()
  val Bson = RiftBson()
  val Xml = RiftXml()
  val MessagePack = RiftMessagePack()
  val Protobuf = RiftProtocolBuffers()
//  val Thrift = RiftThrift()
//  val Yaml = RiftYaml()
  
  def register(memoizer:MemoizesChannels) {
    memoizer.memoizeChannel(Text)
    memoizer.memoizeChannel(Map)
    memoizer.memoizeChannel(Json)
    memoizer.memoizeChannel(Bson)
    memoizer.memoizeChannel(Xml)
    memoizer.memoizeChannel(MessagePack)
    memoizer.memoizeChannel(Protobuf)
  }
}
