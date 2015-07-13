package almhirt.reactivemongox

import scalaz.syntax.validation._
import almhirt.common._

sealed trait ReadWriteMode {
  def isReadOnly: Boolean
  def isWriteOnly: Boolean
  def isReadAndWrite: Boolean
  def tryGetReadPreference: Option[ReadPreferenceAlm]
  def tryGetWriteConcern: Option[WriteConcernAlm]
  def getReadPreference: AlmValidation[ReadPreferenceAlm] =
    tryGetReadPreference match {
      case Some(rp) ⇒ rp.success
      case None     ⇒ NoSuchElementProblem("No read preference").failure
    }
  def getWriteConcern: AlmValidation[WriteConcernAlm] =
    tryGetWriteConcern match {
      case Some(rc) ⇒ rc.success
      case None     ⇒ NoSuchElementProblem("No write concern").failure
    }

  def useForReadOp[T](f: ReadPreferenceAlm ⇒ AlmFuture[T]): AlmFuture[T]
  def useForWriteOp[T](f: WriteConcernAlm ⇒ AlmFuture[T]): AlmFuture[T]
}

object ReadWriteMode {
  sealed trait SupportsReading extends ReadWriteMode
  sealed trait SupportsWriting extends ReadWriteMode

  def apply(readPreference: Option[ReadPreferenceAlm], writeConcern: Option[WriteConcernAlm]): ReadWriteMode =
    (readPreference, writeConcern) match {
      case (None, None)         ⇒ NoReadNoWrite
      case (Some(rp), None)     ⇒ ReadOnly(rp)
      case (None, Some(wc))     ⇒ WriteOnly(wc)
      case (Some(rp), Some(wc)) ⇒ ReadAndWrite(rp, wc)
    }

  case object NoReadNoWrite extends ReadWriteMode {
    override val isReadOnly: Boolean = true
    override val isWriteOnly: Boolean = false
    override val isReadAndWrite: Boolean = false
    override val tryGetReadPreference: Option[ReadPreferenceAlm] = None
    override val tryGetWriteConcern: Option[WriteConcernAlm] = None

    override def useForReadOp[T](f: ReadPreferenceAlm ⇒ AlmFuture[T]): AlmFuture[T] = AlmFuture.failed(IllegalOperationProblem("No reading!"))
    override def useForWriteOp[T](f: WriteConcernAlm ⇒ AlmFuture[T]): AlmFuture[T] = AlmFuture.failed(IllegalOperationProblem("No writing!"))

    override val toString: String = """NoReadNoWrite"""
  }

  final case class ReadOnly(readPreference: ReadPreferenceAlm) extends SupportsReading {
    override def isReadOnly: Boolean = true
    override def isWriteOnly: Boolean = false
    override def isReadAndWrite: Boolean = false
    override def tryGetReadPreference: Option[ReadPreferenceAlm] = Some(readPreference)
    override def tryGetWriteConcern: Option[WriteConcernAlm] = None

    override def useForReadOp[T](f: ReadPreferenceAlm ⇒ AlmFuture[T]): AlmFuture[T] = f(readPreference)
    override def useForWriteOp[T](f: WriteConcernAlm ⇒ AlmFuture[T]): AlmFuture[T] = AlmFuture.failed(IllegalOperationProblem("No writing in read only mode!"))

    override def toString: String = s"""ReadOnly(readPreference=$readPreference)"""
  }

  final case class WriteOnly(writeConcern: WriteConcernAlm) extends SupportsWriting {
    override def isReadOnly: Boolean = false
    override def isWriteOnly: Boolean = true
    override def isReadAndWrite: Boolean = false
    override def tryGetReadPreference: Option[ReadPreferenceAlm] = None
    override def tryGetWriteConcern: Option[WriteConcernAlm] = Some(writeConcern)

    override def useForReadOp[T](f: ReadPreferenceAlm ⇒ AlmFuture[T]): AlmFuture[T] = AlmFuture.failed(IllegalOperationProblem("No reading in write only mode!"))
    override def useForWriteOp[T](f: WriteConcernAlm ⇒ AlmFuture[T]): AlmFuture[T] = f(writeConcern)

    override def toString: String = s"""WriteOnly(writeConcern=$writeConcern)"""
  }

  final case class ReadAndWrite(readPreference: ReadPreferenceAlm, writeConcern: WriteConcernAlm) extends SupportsReading with SupportsWriting {
    override def isReadOnly: Boolean = false
    override def isWriteOnly: Boolean = false
    override def isReadAndWrite: Boolean = true
    override def tryGetReadPreference: Option[ReadPreferenceAlm] = Some(readPreference)
    override def tryGetWriteConcern: Option[WriteConcernAlm] = Some(writeConcern)

    override def useForReadOp[T](f: ReadPreferenceAlm ⇒ AlmFuture[T]): AlmFuture[T] = f(readPreference)
    override def useForWriteOp[T](f: WriteConcernAlm ⇒ AlmFuture[T]): AlmFuture[T] = f(writeConcern)

    override def toString: String = s"""ReadAndWrite(readPreference=$readPreference, writeConcern=$writeConcern)"""
  }
}