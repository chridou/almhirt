package riftwarpx.mongo

import almhirt.serialization.CanSerializeAndDeserialize
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import reactivemongo.bson._
import riftwarp._
import scala.reflect.ClassTag

trait BsonDocumentSerializer[T] {
  def serialize(what: T): AlmValidation[BSONDocument]
  def deserialize(what: BSONDocument): AlmValidation[T]
  def toSerializationFunc = (what: T) => serialize(what)
  def toDeserializationFunc = (what: BSONDocument) => deserialize(what)
}

object BsonDocumentSerializer {
  def apply[T](idLabel: Option[String], riftWarp: RiftWarp)(implicit tag: ClassTag[T]): BsonDocumentSerializer[T] =
    new BsonDocumentSerializer[T] {
      def serialize(what: T): AlmValidation[BSONDocument] =
        for {
          packer <- riftWarp.packers.getFor(what, None, None)
          packed <- packer.packBlind(what)(riftWarp.packers)
          packedWithReplacedId <- idLabel match {
            case Some(idLabel) =>
              packed match {
                case WarpObject(wd, elements) =>
                  elements.find(_.label == idLabel) match {
                    case Some(warpElemForId) =>
                      val newElems = WarpElement("_id", warpElemForId.value) +: elements.filterNot(_.label == idLabel)
                      WarpObject(wd, newElems).success
                    case None =>
                      NoSuchElementProblem(s"""The id label "$idLabel" which will be replaced by "_id" was not found.""").failure
                  }
                case x =>
                  SerializationProblem(s"""Only objects can be serialized to a BSON document. "$x" is not allowed here.""").failure
              }
            case None =>
              packed.success
          }
          res <- ToBsonDematerializer.dematerialize(packedWithReplacedId, Map.empty) match {
            case d: BSONDocument => d.success
            case x => SerializationProblem(s"""The warp package did not dematerialize to a BSON document(which should be impossible...).""").failure
          }
        } yield res

      def deserialize(what: BSONDocument): AlmValidation[T] =
        for {
          rematerializedPackage <- FromBsonRematerializer.rematerialize(what, Map.empty)
          rematerializedObjectWithIdLabel <- rematerializedPackage match {
            case wo: WarpObject =>
              idLabel match {
                case Some(idLabel) =>
                  wo.elements.find(_.label == idLabel) match {
                    case Some(warpElemForId) =>
                      val newElems = WarpElement("_id", warpElemForId.value) +: wo.elements.filterNot(_.label == idLabel)
                      WarpObject(wo.warpDescriptor, newElems).success
                    case None =>
                      NoSuchElementProblem(s"""The id label "$idLabel" which will be replaced by "_id" was not found.""").failure
                  }
                case None => wo.success
              }
            case x =>
              SerializationProblem(s"""The BSON did not rematerialize to an object.""").failure
          }
          unpacker <- rematerializedObjectWithIdLabel.warpDescriptor match {
            case Some(wd) =>
              riftWarp.unpackers.get(wd)
            case None =>
              riftWarp.unpackers.getByTag(tag)
          }
          unpackedUntyped <- unpacker.unpack(rematerializedObjectWithIdLabel)(riftWarp.unpackers)
          unpackedTyped <- unpackedUntyped.castTo[T]
        } yield unpackedTyped
    }
}