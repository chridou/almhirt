package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait RiftWarpFuns {
  def prepareDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], packers: WarpPackers, dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] =
    packer.pack(what).map(pkg ⇒ (dematerializer.dematerialize(pkg, options), packer.warpDescriptor))

  def prepareFlatDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] =
    packer.pack(what)(WarpPackers.NoWarpPackers).map(pkg ⇒ (dematerializer.dematerialize(pkg, options), packer.warpDescriptor))

  def prepareFreeDeparture[U](what: Any, overrideDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit packers: WarpPackers, dematerializer: Dematerializer[U]): AlmValidation[(U, WarpDescriptor)] = {
    val wd = overrideDescriptor.getOrElse(WarpDescriptor(what.getClass()))
    packers.getFor(what, None, None).flatMap(packer ⇒
      packer.packBlind(what).map(pkg ⇒
        (dematerializer.dematerialize(pkg, options), packer.warpDescriptor)))
  }

  def handleArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T], unpackers: WarpUnpackers): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg ⇒ unpacker.unpack(pkg))

  def handleFlatArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T]): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg ⇒ unpacker.unpack(pkg)(WarpUnpackers.NoWarpUnpackers))

  def handleFreeArrival[U](from: U, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpackers: WarpUnpackers): AlmValidation[Any] =
    rematerializer.rematerialize(from, options).flatMap(pkg ⇒ unpack(pkg, overrideDescriptor, backUpDescriptor))

  def handleTypedArrival[U, T](from: U, overrideDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    rematerializer.rematerialize(from, options).flatMap(pkg ⇒
      unpack(pkg, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(res ⇒
        res.castTo[T]))

  def handleFreeArrivalWith(from: Any, rematerializer: Rematerializer[Any], overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None, options: Map[String, Any] = Map.empty)(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    rematerializer.rematerialize(from, options).flatMap(pkg ⇒ unpack(pkg, overrideDescriptor, backUpDescriptor))

  def unpack(what: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Any] = {
    overrideDescriptor match {
      case Some(pd) ⇒
        unpackers.get(pd).leftMap(old ⇒ NoSuchElementProblem(s"WarpDescriptor has been overriden", cause = Some(old))).flatMap(_(what))
      case None ⇒
        what match {
          case wp: WarpPrimitive ⇒ wp.value.success
          case WarpObject(td, _) ⇒
            td match {
              case Some(d) ⇒
                unpackers.get(d).fold(
                  fail ⇒ backUpDescriptor match {
                    case Some(bud) ⇒ unpackers.get(bud).flatMap(_(what))
                    case None ⇒ SerializationProblem(s"""No Unpacker found for WarpObject. Hint: The WarpObject had a ${d.toString} but no unpacker was found. There was neither a backup WarpDescriptor nor an override WarpDescriptor.""").failure
                  },
                  unpacker ⇒ unpacker(what))
              case None ⇒
                backUpDescriptor match {
                  case Some(bud) ⇒ unpackers.get(bud).flatMap(_(what))
                  case None ⇒ SerializationProblem("No Unpacker found for WarpObject. Hint: Neither the WarpObject contained a WarpDescriptor nor a backup WarpDescriptor or override WarpDescriptor were supplied.").failure
                }
            }
          case bp: BinaryWarpPackage ⇒ bp.bytes.success
          case WarpCollection(items) ⇒
            val x = items.map(item ⇒ unpack(item, None, None).toAgg).sequence
            x
          case WarpAssociativeCollection(items) ⇒
            val x = items.map(item ⇒
              unpack(item._1, None, None).flatMap(k ⇒
                unpack(item._2, None, None).map(v ⇒
                  (k, v))).toAgg).sequence
            x
          case WarpTuple2(a, b) ⇒
            for {
              va ← unpack(a, None, None)
              vb ← unpack(b, None, None)
            } yield (va, vb)
          case WarpTuple3(a, b, c) ⇒
            for {
              va ← unpack(a, None, None)
              vb ← unpack(b, None, None)
              vc ← unpack(c, None, None)
            } yield (va, vb, vc)
          case WarpTree(tree) ⇒
            val x = tree.map(item ⇒ unpack(item, None, None).toAgg).sequence
            x
        }
    }
  }

}