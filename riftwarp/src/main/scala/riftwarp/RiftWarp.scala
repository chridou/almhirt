package riftwarp

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import almhirt.http.HttpContent
import almhirt.http.{ HttpMarshaller, HttpUnmarshaller, HttpContent, ClassifiesChannels }
import riftwarp.std.RiftWarpFuns
import almhirt.http.HasHttpMarshallers
import almhirt.http.HasHttpUnmarshallers

trait RiftWarp {
  private object myFuns extends RiftWarpFuns

  def packers: WarpPackers
  def unpackers: WarpUnpackers
  def dematerializers: Dematerializers
  def rematerializers: Rematerializers

  def departure(dimension: String, channel: String, what: Any, options: Map[String, Any] = Map.empty): AlmValidation[(Any, WarpDescriptor)] =
    for {
      packer <- packers.getFor(what, None, None)
      packed <- packer.packBlind(what)(packers)
      dematerialize <- dematerializers.get(dimension, channel)
    } yield (dematerialize(packed, options), packer.warpDescriptor)

  def departureTyped[TDim](channel: String, what: Any, options: Map[String, Any] = Map.empty)(implicit tag: ClassTag[TDim]): AlmValidation[(TDim, WarpDescriptor)] =
    departure(tag.runtimeClass.getName(), channel, what, options).flatMap(x => x._1.castTo[TDim].map((_, x._2)))

  def httpDeparture(channel: String, what: Any, options: Map[String, Any] = Map.empty)(implicit classifies: ClassifiesChannels): AlmValidation[HttpContent] =
    myFuns.prepareHttpDeparture(channel, what, None, None, options)(packers, dematerializers, classifies)
    
  def arrival(dimension: String, channel: String, from: Any, options: Map[String, Any] = Map.empty): AlmValidation[Any] =
    for {
      rematerialize <- rematerializers.get(dimension, channel)
      arrived <- myFuns.handleFreeArrivalWith(from, rematerialize, None, None, options)(unpackers)
    } yield arrived

  def arrivalTyped[TDim, U](channel: String, from: TDim, options: Map[String, Any] = Map.empty)(implicit tagDim: ClassTag[TDim], tagTarget: ClassTag[U]): AlmValidation[U] =
    for {
      fromTyped <- from.castTo[TDim]
      rematerialized <- rematerializers.rematerializeTyped[TDim](channel, fromTyped, options)
      arrived <- myFuns.unpack(rematerialized, None, None)(unpackers)
      arrivedTyped <- arrived.castTo[U]
    } yield arrivedTyped

  def httpArrival[T](from: HttpContent, options: Map[String, Any] = Map.empty)(implicit tag: ClassTag[T]): AlmValidation[T] =
    myFuns.handleHttpArrival[T](from, options)(rematerializers, unpackers, tag)
}

object RiftWarp {
  def apply(): RiftWarp = apply(WarpPackers(), WarpUnpackers(), Dematerializers(), Rematerializers())
  def apply(thePackers: WarpPackers, theUnpackers: WarpUnpackers): RiftWarp = apply(thePackers, theUnpackers, Dematerializers(), Rematerializers())
  def apply(thePackers: WarpPackers, theUnpackers: WarpUnpackers, theDematerializers: Dematerializers, theRematerializers: Rematerializers): RiftWarp = new RiftWarp {
    val packers = thePackers
    val unpackers = theUnpackers
    val dematerializers = theDematerializers
    val rematerializers = theRematerializers
  }

  def empty: RiftWarp = new RiftWarp {
    val packers = WarpPackers.empty
    val unpackers = WarpUnpackers.empty
    val dematerializers = Dematerializers.empty
    val rematerializers = Rematerializers.empty

  }

  implicit class RiftWarpOps(self: RiftWarp) {
    def createHttpMarshaller[T: ClassTag](options: Map[String, Any] = Map.empty)(implicit classifies: ClassifiesChannels): HttpMarshaller[T] = 
      new HttpMarshaller[T] {
      override def marshal(from: T, toChannel: String): AlmValidation[HttpContent] = self.httpDeparture(toChannel, from, options)
    }
    def createHttpUnmarshaller[T: ClassTag](options: Map[String, Any] = Map.empty): HttpUnmarshaller[T] =
      new HttpUnmarshaller[T] {
        override def unmarshal(from: HttpContent) = self.httpArrival[T](from, options)
      }

    def createMarschallingFactory(classifies: ClassifiesChannels, options: Map[String, Any] = Map.empty): HasHttpMarshallers with HasHttpUnmarshallers =
      new HasHttpMarshallers with HasHttpUnmarshallers {
		  def getMarschaller[T](implicit tag: ClassTag[T]): AlmValidation[HttpMarshaller[T]] = createHttpMarshaller[T](options)(tag, classifies).success
		  def getUnmarschaller[T](implicit tag: ClassTag[T]): AlmValidation[HttpUnmarshaller[T]] = createHttpUnmarshaller[T](options).success
      }
  }
}