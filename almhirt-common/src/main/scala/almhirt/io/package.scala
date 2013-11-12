package almhirt

package object io {
  implicit val CanWriteByteIntoBinaryWriterInst = CanWriteByteIntoBinaryWriter
  implicit val CanWriteShortIntoBinaryWriterInst = CanWriteShortIntoBinaryWriter
  implicit val CanWriteIntIntoBinaryWriterInst = CanWriteIntIntoBinaryWriter
  implicit val CanWriteLongIntoBinaryWriterInst = CanWriteLongIntoBinaryWriter
}