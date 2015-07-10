package almhirt.reactivemongox

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.configuration._
import reactivemongo.api.commands.{ WriteConcern, GetLastError }
import com.typesafe.config.Config
import reactivemongo.api.commands.GetLastError

sealed trait WriteConcernAlm
object WriteConcernAlm {
  def apply(w: Int, journaled: Boolean, fsync: Boolean, timeout: Option[FiniteDuration]): WriteConcernAlm = {
    if (w == 0 && journaled == false) {
      Unacknowledged(fsync)
    } else if (w == 1 && journaled == false) {
      Acknowledged(fsync)
    } else if (w == 1 && journaled == true) {
      Journaled(fsync)
    } else {
      if (journaled)
        ReplicaJournaled(w, timeout, fsync)
      else
        ReplicaAcknowledged(w, timeout, fsync)
    }
  }

  /**
   * With an unacknowledged write concern, MongoDB does not acknowledge the receipt of write operations.
   * Unacknowledged is similar to errors ignored; however, drivers will attempt to receive and handle network errors when possible.
   * The driver’s ability to detect network errors depends on the system’s networking configuration.
   */
  final case class Unacknowledged(fsync: Boolean) extends WriteConcernAlm {
    override def toString = s"Unacknowledged(fsync=$fsync)"
  }

  /**
   * With a receipt acknowledged write concern, the mongod confirms that it received the write operation and applied the change to the in-memory view of data.
   * Acknowledged write concern allows clients to catch network, duplicate key, and other errors.
   */
  final case class Acknowledged(fsync: Boolean) extends WriteConcernAlm {
    override def toString = s"Acknowledged(fsync=$fsync)"
  }

  /**
   * With a journaled write concern, the MongoDB acknowledges the write operation only after committing the data to the journal.
   * This write concern ensures that MongoDB can recover the data following a shutdown or power interruption.
   *
   * You must have journaling enabled to use this write concern.
   */
  final case class Journaled(fsync: Boolean) extends WriteConcernAlm {
    override def toString = s"Journaled(fsync=$fsync)"
  }

  final case class ReplicaAcknowledged(w: Int, wtimeout: Option[FiniteDuration], fsync: Boolean) extends WriteConcernAlm {
    override def toString = s"ReplicaAcknowledged(w=$w, wtimeout=$wtimeout, fsync=$fsync)"
  }

  final case class ReplicaJournaled(w: Int, wtimeout: Option[FiniteDuration], fsync: Boolean) extends WriteConcernAlm {
    override def toString = s"ReplicaJournaled(w=$w, wtimeout=$wtimeout, fsync=$fsync)"
  }

  val Default = Acknowledged(false)

  def toReactiveMongoWriteConcern(what: WriteConcernAlm): WriteConcern = {
    what match {
      case Unacknowledged(fsync) ⇒
        GetLastError(w = GetLastError.WaitForAknowledgments(0), j = false, fsync = fsync, wtimeout = None)
      case Acknowledged(fsync) ⇒
        GetLastError(w = GetLastError.WaitForAknowledgments(1), j = false, fsync = fsync, wtimeout = None)
      case Journaled(fsync) ⇒
        GetLastError(w = GetLastError.WaitForAknowledgments(1), j = true, fsync = fsync, wtimeout = None)
      case ReplicaAcknowledged(w, timeout, fsync) ⇒
        val n = if (w < 2) 2 else w
        GetLastError(w = GetLastError.WaitForAknowledgments(n), j = false, fsync = fsync, wtimeout = timeout.map(_.toMillis.toInt))
      case ReplicaJournaled(w, timeout, fsync) ⇒
        val n = if (w < 2) 2 else w
        GetLastError(w = GetLastError.WaitForAknowledgments(n), j = true, fsync = fsync, wtimeout = timeout.map(_.toMillis.toInt))

    }
  }

  def fromConfig(config: Config): AlmValidation[WriteConcernAlm] = {
    for {
      mode ← config.v[String]("mode")
      res ← mode match {
        case "unacknowledged"       ⇒ unacknowledgedFromConfig(config).leftMap { p => ConfigurationProblem(s"""Failed to configure write concern "unacknowledged".""", cause = Some(p)) }
        case "acknowledged"         ⇒ acknowledgedFromConfig(config).leftMap { p => ConfigurationProblem(s"""Failed to configure write concern "acknowledged".""", cause = Some(p)) }
        case "journaled"            ⇒ journaledFromConfig(config).leftMap { p => ConfigurationProblem(s"""Failed to configure write concern "journaled".""", cause = Some(p)) }
        case "replica-acknowledged" ⇒ replicaAcknowledgedFromConfig(config).leftMap { p => ConfigurationProblem(s"""Failed to configure write concern "replica-acknowledged".""", cause = Some(p)) }
        case "replica-journaled"    ⇒ replicaJournaledFromConfig(config).leftMap { p => ConfigurationProblem(s"""Failed to configure write concern "replica-journaled".""", cause = Some(p)) }
        case x                      ⇒ ConfigurationProblem(s""""$x" is not a valid mode for a write concern. Allowed are: unacknowledged, acknowledged, journaled, replica-acknowledged, replica-journaled.""").failure
      }
    } yield res
  }

  private def unacknowledgedFromConfig(config: Config): AlmValidation[Unacknowledged] =
    for {
      fsync ← config.opt[Boolean]("fsync").map(_ getOrElse false)
    } yield Unacknowledged(fsync)

  private def acknowledgedFromConfig(config: Config): AlmValidation[Acknowledged] =
    for {
      fsync ← config.opt[Boolean]("fsync").map(_ getOrElse false)
    } yield Acknowledged(fsync)

  private def journaledFromConfig(config: Config): AlmValidation[Journaled] =
    for {
      fsync ← config.opt[Boolean]("fsync").map(_ getOrElse false)
    } yield Journaled(fsync)

  private def replicaAcknowledgedFromConfig(config: Config): AlmValidation[ReplicaAcknowledged] =
    for {
      fsync ← config.opt[Boolean]("fsync").map(_ getOrElse false)
      w ← config.v[Int]("w")
      wVal ← if (w > 1) w.success else ConstraintViolatedProblem(s""""w" must be greater than 1. Got $w.""").failure
      timeout ← config.opt[FiniteDuration]("timeout")
    } yield ReplicaAcknowledged(wVal, timeout, fsync)

  private def replicaJournaledFromConfig(config: Config): AlmValidation[ReplicaJournaled] =
    for {
      fsync ← config.opt[Boolean]("fsync").map(_ getOrElse false)
      w ← config.v[Int]("w")
      wVal ← if (w > 1) w.success else ConstraintViolatedProblem(s""""w" must be greater than 1. Got $w.""").failure
      timeout ← config.opt[FiniteDuration]("timeout")
    } yield ReplicaJournaled(wVal, timeout, fsync)

}