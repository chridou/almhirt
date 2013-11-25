package riftwarp

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicReference
import scalaz._, Scalaz._
import almhirt.common._

sealed trait WireTransmission
case object NoWireTransmission extends WireTransmission
case object WireTransmissionAsBinary extends WireTransmission
case object WireTransmissionAsText extends WireTransmission

trait WarpChannel {
  def channelDescriptor: String
  def wireTransmission: WireTransmission
  def schemaless: Boolean
}

object WarpChannel {
  def apply(theChannelDescriptor: String, theWireTransmission: WireTransmission, isSchemaless: Boolean) = new WarpChannel {
    val channelDescriptor = theChannelDescriptor
    val wireTransmission = theWireTransmission
    val schemaless = isSchemaless
  }

  def overWireAsText(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, WireTransmissionAsText, isSchemaless)

  def overWireAsBinary(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, WireTransmissionAsBinary, isSchemaless)

  def notWireCapable(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, NoWireTransmission, isSchemaless)
}

object WarpChannels {
  private[this] val myChannels = new AtomicReference(Map.empty[String, WarpChannel])

  @tailrec
  def register(channel: WarpChannel) {
    val current = myChannels.get
    val updated = current.updated(channel.channelDescriptor, channel)
    if (!myChannels.compareAndSet(current, updated)) register(channel)
  }
  
  
  def registeredOverWireAsText(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overWireAsText(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }
 
  def registeredOverWireAsBinary(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overWireAsBinary(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }

  def registeredNotWireCapable(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overWireAsText(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }
  
  def getChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    myChannels.get().get(channelDescriptor) match {
      case Some(channel) => channel.success
      case None => NoSuchElementProblem(s""""$channelDescriptor" is not registered.""").failure
    }
  }

  def getBinaryChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    getChannel(channelDescriptor).flatMap(ch =>
      ch.wireTransmission match {
        case WireTransmissionAsBinary => ch.success
        case NoWireTransmission => UnspecifiedProblem(s""""$channelDescriptor" is not transmittable over a wire.""").failure
        case WireTransmissionAsText => UnspecifiedProblem(s""""$channelDescriptor" is not a binary channel. It is a text channel.""").failure
      })
  }
  
  def getTextChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    getChannel(channelDescriptor).flatMap(ch =>
      ch.wireTransmission match {
        case WireTransmissionAsText => ch.success
        case NoWireTransmission => UnspecifiedProblem(s""""$channelDescriptor" is not transmittable over a wire.""").failure
        case WireTransmissionAsBinary => UnspecifiedProblem(s""""$channelDescriptor" is not a text channel. It is a binary channel""").failure
      })
  }
  
  val `rift-json` = registeredOverWireAsText("json", true)
  val `rift-xml` = registeredOverWireAsText("xml", true)
  val `rift-text` = registeredOverWireAsText("text", true)
  val `rift-html` = registeredOverWireAsText("html", true)

  val `rift-msgpack` = registeredOverWireAsBinary("msgpack", true)
  val `rift-bson` = registeredOverWireAsBinary("bson", true)

  val `rift-json-cord` = registeredNotWireCapable("json-cord", true)
  val `rift-html-std` = registeredNotWireCapable("json-cord", true)
  val `rift-json-std` = registeredNotWireCapable("json-std", true)
  val `rift-xml-std` = registeredNotWireCapable("xml-std", true)
  val `rift-json-spray` = registeredNotWireCapable("json-spray", true)
  val `rift-bson-reactive-mongo` = registeredNotWireCapable("bson-reactive-mongo", true)
  val `rift-exploded` = registeredNotWireCapable("exploded", true)
  val `rift-package` = registeredNotWireCapable("package", true)

  
}

