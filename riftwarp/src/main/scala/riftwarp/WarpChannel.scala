package riftwarp

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicReference
import scalaz._, Scalaz._
import almhirt.common._

sealed trait HttpTransmission
case object NoHttpTransmission extends HttpTransmission
case object HttpTransmissionAsBinary extends HttpTransmission
case object HttpTransmissionAsText extends HttpTransmission

trait WarpChannel {
  def channelDescriptor: String
  def HttpTransmission: HttpTransmission
  def schemaless: Boolean
}

object WarpChannel {
  def apply(theChannelDescriptor: String, theHttpTransmission: HttpTransmission, isSchemaless: Boolean) = new WarpChannel {
    val channelDescriptor = theChannelDescriptor
    val HttpTransmission = theHttpTransmission
    val schemaless = isSchemaless
  }

  def overHttpAsText(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, HttpTransmissionAsText, isSchemaless)

  def overHttpAsBinary(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, HttpTransmissionAsBinary, isSchemaless)

  def notHttpCapable(theChannelDescriptor: String, isSchemaless: Boolean) =
    WarpChannel(theChannelDescriptor, NoHttpTransmission, isSchemaless)
}

object WarpChannels {
  private[this] val myChannels = new AtomicReference(Map.empty[String, WarpChannel])

  @tailrec
  def register(channel: WarpChannel) {
    val current = myChannels.get
    val updated = current.updated(channel.channelDescriptor, channel)
    if (!myChannels.compareAndSet(current, updated)) register(channel)
  }
  
  
  def registeredOverHttpAsText(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overHttpAsText(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }
 
  def registeredOverHttpAsBinary(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overHttpAsBinary(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }

  def registeredNotHttpCapable(theChannelDescriptor: String, isSchemaless: Boolean): WarpChannel = {
    val ch = WarpChannel.overHttpAsText(theChannelDescriptor: String, isSchemaless: Boolean)
    register(ch)
    ch
  }
  
  def getChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    myChannels.get().get(channelDescriptor) match {
      case Some(channel) ⇒ channel.success
      case None ⇒ NoSuchElementProblem(s""""$channelDescriptor" is not registered.""").failure
    }
  }

  def getBinaryChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    getChannel(channelDescriptor).flatMap(ch ⇒
      ch.HttpTransmission match {
        case HttpTransmissionAsBinary ⇒ ch.success
        case NoHttpTransmission ⇒ UnspecifiedProblem(s""""$channelDescriptor" is not transmittable over a Http.""").failure
        case HttpTransmissionAsText ⇒ UnspecifiedProblem(s""""$channelDescriptor" is not a binary channel. It is a text channel.""").failure
      })
  }
  
  def getTextChannel(channelDescriptor: String) : AlmValidation[WarpChannel] = {
    getChannel(channelDescriptor).flatMap(ch ⇒
      ch.HttpTransmission match {
        case HttpTransmissionAsText ⇒ ch.success
        case NoHttpTransmission ⇒ UnspecifiedProblem(s""""$channelDescriptor" is not transmittable over a Http.""").failure
        case HttpTransmissionAsBinary ⇒ UnspecifiedProblem(s""""$channelDescriptor" is not a text channel. It is a binary channel""").failure
      })
  }
  
  val `rift-json` = registeredOverHttpAsText("json", true)
  val `rift-xml` = registeredOverHttpAsText("xml", true)
  val `rift-text` = registeredOverHttpAsText("text", true)
  val `rift-html` = registeredOverHttpAsText("html", true)

  val `rift-msgpack` = registeredOverHttpAsBinary("msgpack", true)
  val `rift-bson` = registeredOverHttpAsBinary("bson", true)

  val `rift-json-cord` = registeredNotHttpCapable("json-cord", true)
  val `rift-html-std` = registeredNotHttpCapable("json-cord", true)
  val `rift-json-std` = registeredNotHttpCapable("json-std", true)
  val `rift-xml-std` = registeredNotHttpCapable("xml-std", true)
  val `rift-json-spray` = registeredNotHttpCapable("json-spray", true)
  val `rift-bson-reactive-mongo` = registeredNotHttpCapable("bson-reactive-mongo", true)
  val `rift-exploded` = registeredNotHttpCapable("exploded", true)
  val `rift-package` = registeredNotHttpCapable("package", true)

  
}

