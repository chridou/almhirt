package riftwarp.std

import almhirt.common._
import riftwarp._

trait RiftWarpFuns {
  def prepareDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], packers: WarpPackers, dematerializer: Dematerializer[U]): AlmValidation[U] =
    packer.pack(what).map(pkg => dematerializer.dematerialize(pkg, options))

  def prepareFlatDeparture[T, U](what: T, options: Map[String, Any] = Map.empty)(implicit packer: WarpPacker[T], dematerializer: Dematerializer[U]): AlmValidation[U] =
    packer.pack(what)(WarpPackers.NoWarpPackers).map(pkg => dematerializer.dematerialize(pkg, options))
  
  def handleArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T], unpackers: WarpUnpackers): AlmValidation[T] =  
    rematerializer.rematerialize(from, options).flatMap(pkg => unpacker.unpack(pkg))

  def handleFlatArrival[U, T](from: U, options: Map[String, Any] = Map.empty)(implicit rematerializer: Rematerializer[U], unpacker: WarpUnpacker[T]): AlmValidation[T] =  
    rematerializer.rematerialize(from, options).flatMap(pkg => unpacker.unpack(pkg)(WarpUnpackers.NoWarpUnpackers))
    
}