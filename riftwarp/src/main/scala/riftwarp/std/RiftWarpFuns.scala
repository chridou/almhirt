package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http._
import riftwarp._

trait RiftWarpFuns {
  def prepareDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], packers: WarpPackers, dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] =
    packer.pack(what).map(pkg => (dematerializer.dematerialize(pkg, options), packer.warpDescriptor))

  def prepareFlatDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] =
    packer.pack(what)(WarpPackers.NoWarpPackers).map(pkg => (dematerializer.dematerialize(pkg, options), packer.warpDescriptor))

  def prepareFreeDeparture[U](what: Any, overrideDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit packers: WarpPackers, dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] = {
    val wd = overrideDescriptor.getOrElse(WarpDescriptor(what.getClass()))
    packers.getFor(what, None, None).flatMap(packer =>
      packer.packBlind(what).map(pkg =>
        (dematerializer.dematerialize(pkg, options), packer.warpDescriptor)))
  }

  def prepareHttpDeparture(channel: String, what: Any, overrideDescriptor: Option[WarpDescriptor] = None, backupDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit packers: WarpPackers, dematerializers: Dematerializers, classifies: ClassifiesChannels): AlmValidation[HttpContent] =
    for {
      packer <- packers.getFor(what, overrideDescriptor, backupDescriptor)
      dimensionClassifier <- classifies(channel)
      dematerializer <- dematerializers.get(dimensionClassifier.transportType.getName(), channel)
      packed <- packer.packBlind(what)
      dematerialized <- dematerializer.apply(packed, options).success
      contentType <- packer.warpDescriptor.version match {
        case Some(version) => HttpContentType(s"${packer.warpDescriptor.identifier}+$channel", Map("version" -> version.toString)).success
        case None => HttpContentType(s"${packer.warpDescriptor.identifier}+$channel", Map.empty).success
      }
      payload <- dimensionClassifier match {
        case BinaryChannel => dematerialized.castTo[Array[Byte]].map(BinaryPayload(_))
        case TextChannel => dematerialized.castTo[String].map(TextPayload(_))
      }
    } yield HttpContent(contentType, payload)

  def handleArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T], unpackers: WarpUnpackers): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg => unpacker.unpack(pkg))

  def handleFlatArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T]): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg => unpacker.unpack(pkg)(WarpUnpackers.NoWarpUnpackers))

  def handleFreeArrival[U](from: U, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpackers: WarpUnpackers): AlmValidation[Any] =
    rematerializer.rematerialize(from, options).flatMap(pkg => unpack(pkg, overrideDescriptor, backUpDescriptor))

  def handleTypedArrival[U, T](from: U, overrideDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg =>
      unpack(pkg, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(res =>
        res.castTo[T]))

  def handleFreeArrivalWith(from: Any, rematerialize: (Any, Map[String, Any]) => AlmValidation[WarpPackage], overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    rematerialize(from, options).flatMap(pkg => unpack(pkg, overrideDescriptor, backUpDescriptor))

  def handleHttpArrival[T](from: HttpContent, options: Map[String, Any] = Map.empty)(implicit rematerializers: Rematerializers, unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    for {
      channelAndDescriptor <- from.contentType.channelAndDescriptor
      rematerializer <- rematerializers.get(from.payload.data.getClass.getName(), channelAndDescriptor._1)
      result <- handleFreeArrivalWith(from.payload.data, rematerializer, None, channelAndDescriptor._2, options)
      typedResult <- result.castTo[T]
    } yield (typedResult)

  def unpack(what: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Any] = {
    overrideDescriptor match {
      case Some(pd) =>
        unpackers.get(pd).leftMap(_.mapMessage(old => s"WarpDescriptor has been overriden: $old")).flatMap(_(what))
      case None =>
        what match {
          case wp: WarpPrimitive => wp.value.success
          case WarpObject(td, _) =>
            td match {
              case Some(d) =>
                unpackers.get(d).fold(
                  fail => backUpDescriptor match {
                    case Some(bud) => unpackers.get(bud).flatMap(_(what))
                    case None => SerializationProblem("No Unpacker found for WarpObject. Hint: The WarpObject had a WarpDescriptor but no unpacker was found. There was neither a backup WarpDescriptor nor an override WarpDescriptor.").failure
                  },
                  unpacker => unpacker(what))
              case None =>
                backUpDescriptor match {
                  case Some(bud) => unpackers.get(bud).flatMap(_(what))
                  case None => SerializationProblem("No Unpacker found for WarpObject. Hint: Neither the WarpObject contained a WarpDescriptor nor a backup WarpDescriptor or override WarpDescriptor were supplied.").failure
                }
            }
          case bp: BinaryWarpPackage => bp.bytes.success
          case WarpCollection(items) =>
            val x = items.map(item => unpack(item, None, None).toAgg).sequence
            x
          case WarpAssociativeCollection(items) =>
            val x = items.map(item =>
              unpack(item._1, None, None).flatMap(k =>
                unpack(item._2, None, None).map(v =>
                  (k, v))).toAgg).sequence
            x
          case WarpTree(tree) =>
            val x = tree.map(item => unpack(item, None, None).toAgg).sequence
            x
        }
    }
  }

}