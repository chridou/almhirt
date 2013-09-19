package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Deadline
import akka.actor._
import almhirt.domain.AggregateRootCell._
import almhirt.components.AggregateRootCellSource.CellHandle

case class AggregateRootCellCacheStats(
  numCells: Long,
  numBookings: Long,
  numBookedArs: Long,
  numUninitialized: Long,
  numLoaded: Long,
  numDoesNotExist: Long,
  numErrorneous: Long,
  numBookingsInLifetime: Long,
  numUnbookingsInLifetime: Long,
  numDeathCertificatesStillDue: Long,
  kills: Long,
  confirmedKills: Long) {
  def toNiceString: String = {
    s"""|Aggregate root cell cache statistics
    	|
    	|cells                        = $numCells
    	|bookings                     = $numBookings
    	|booked cells                 = $numBookedArs
    	|
    	|uninitialized                = $numUninitialized
    	|loaded                       = $numLoaded
    	|doesNotExist                 = $numDoesNotExist
    	|error state                  = $numErrorneous
    	|
    	|bookings in lifetime         = $numBookingsInLifetime
    	|unbookings in lifetime       = $numUnbookingsInLifetime
    	|booking diff                 = ${numBookingsInLifetime - numUnbookingsInLifetime}
    	|
    	|death certificates still due = $numDeathCertificatesStillDue
    	|kills                        = $kills
    	|confirmedKills               = $confirmedKills""".stripMargin
  }

  def -(other: AggregateRootCellCacheStats): AggregateRootCellCacheStats =
    AggregateRootCellCacheStats(
      numCells - other.numCells,
      numBookings - other.numBookings,
      numBookedArs - other.numBookedArs,
      numUninitialized - other.numUninitialized,
      numLoaded - other.numLoaded,
      numDoesNotExist - other.numDoesNotExist,
      numErrorneous - other.numErrorneous,
      numBookingsInLifetime - other.numBookingsInLifetime,
      numUnbookingsInLifetime - other.numUnbookingsInLifetime,
      numDeathCertificatesStillDue - other.numDeathCertificatesStillDue,
      kills - other.kills,
      confirmedKills - other.confirmedKills)

  def toNiceDiffStringWith(other: AggregateRootCellCacheStats, titleHint: String = "difference"): String = {
    val diff = this - other
    s"""|Aggregate root cell cache statistics($titleHint)
       	|
    	|cells                        = $numCells - ${other.numCells} = ${diff.numCells}
        |bookings                     = $numBookings - ${other.numBookings} = ${diff.numBookings}
       	|booked cells                 = $numBookedArs - ${other.numBookedArs} = ${diff.numBookedArs}
       	|
       	|uninitialized                = $numUninitialized - ${other.numUninitialized} = ${diff.numUninitialized}
    	|loaded                       = $numLoaded - ${other.numLoaded} = ${diff.numLoaded}
    	|doesNotExist                 = $numDoesNotExist - ${other.numDoesNotExist} = ${diff.numDoesNotExist}
    	|error state                  = $numErrorneous - ${other.numErrorneous} = ${diff.numErrorneous}
    	|
    	|bookings in lifetime         = $numBookingsInLifetime - ${other.numBookingsInLifetime} = ${diff.numBookingsInLifetime} 
    	|unbookings in lifetime       = $numUnbookingsInLifetime - ${other.numUnbookingsInLifetime} = ${diff.numUnbookingsInLifetime}
    	|booking diff                 = ${numBookingsInLifetime - numUnbookingsInLifetime} - ${other.numBookingsInLifetime - other.numUnbookingsInLifetime} = ${(numBookingsInLifetime - numUnbookingsInLifetime) - (other.numBookingsInLifetime - other.numUnbookingsInLifetime)}
    	|
    	|death certificates still due = $numDeathCertificatesStillDue - ${other.numDeathCertificatesStillDue} = ${diff.numDeathCertificatesStillDue}
    	|kills                        = $kills - ${other.kills} = ${diff.kills}
    	|confirmedKills               = $confirmedKills - ${other.confirmedKills} = ${diff.confirmedKills}""".stripMargin
  }
}

object AggregateRootCellCacheStats {
	def tripletComparisonString(a: AggregateRootCellCacheStats, b: AggregateRootCellCacheStats, c: AggregateRootCellCacheStats, nameA: String = "A", nameB: String = "B", nameC: String = "C"): String =
    s"""|Aggregate root cell cache statistics($nameA/$nameB/$nameC)
    	|
    	|cells                        = ${a.numCells}/${b.numCells}/${c.numCells}
    	|bookings                     = ${a.numBookings}/${b.numBookings}/${c.numBookings}
    	|booked cells                 = ${a.numBookedArs}/${b.numBookedArs}/${c.numBookedArs}
    	|
    	|uninitialized                = ${a.numUninitialized}/${b.numUninitialized}/${c.numUninitialized}
    	|loaded                       = ${a.numLoaded}/${b.numLoaded}/${c.numLoaded}
    	|doesNotExist                 = ${a.numDoesNotExist}/${b.numDoesNotExist}/${c.numDoesNotExist}
    	|error state                  = ${a.numErrorneous}/${b.numErrorneous}/${c.numErrorneous}
    	|
    	|bookings in lifetime         = ${a.numBookingsInLifetime}/${b.numBookingsInLifetime}/${c.numBookingsInLifetime}
    	|unbookings in lifetime       = ${a.numUnbookingsInLifetime}/${b.numUnbookingsInLifetime}/${c.numUnbookingsInLifetime}
    	|booking diff                 = ${a.numBookingsInLifetime - a.numUnbookingsInLifetime}/${b.numBookingsInLifetime - b.numUnbookingsInLifetime}/${c.numBookingsInLifetime - c.numUnbookingsInLifetime}
    	|
    	|death certificates still due = ${a.numDeathCertificatesStillDue}/${b.numDeathCertificatesStillDue}/${c.numDeathCertificatesStillDue}
    	|kills                        = ${a.kills}/${b.kills}/${c.kills}
    	|confirmedKills               = ${a.confirmedKills}/${b.confirmedKills}/${c.confirmedKills}""".stripMargin
}

object MutableAggregateRootCellCache {
  sealed trait BookingResult
  final case class CellBooked(handle: CellHandle) extends BookingResult
  case object AwaitingDeathCertificate extends BookingResult
}

class MutableAggregateRootCellCache(
  createCell: (JUUID, Class[_]) => ActorRef,
  killCell: ActorRef => Unit,
  requestUnbook: Long => Unit) {

  import MutableAggregateRootCellCache._

  private val cellByArId = scala.collection.mutable.HashMap.empty[JUUID, ActorRef]
  private val bookingsByArId = scala.collection.mutable.HashMap.empty[JUUID, scala.collection.mutable.HashSet[Long]]
  private val arIdByBookingId = scala.collection.mutable.HashMap.empty[Long, JUUID]

  private val uninitializedCells = scala.collection.mutable.HashMap.empty[JUUID, Deadline]
  private val loadedCells = scala.collection.mutable.HashMap.empty[JUUID, Deadline]
  private val doesNotExistCells = scala.collection.mutable.HashMap.empty[JUUID, Deadline]
  private val errorneousCells = scala.collection.mutable.HashMap.empty[JUUID, Deadline]
  private val deathCertificatesStillDueById = scala.collection.mutable.HashMap.empty[JUUID, ActorRef]
  private val deathCertificatesStillDueByCell = scala.collection.mutable.HashMap.empty[ActorRef, JUUID]
  private var nextHandleId = 1L
  private var numUnbookings = 0L
  private var kills = 0L
  private var confirmedKills = 0L

  def bookCell(arId: JUUID, arType: Class[_], onStrangeState: String => Unit): (BookingResult, MutableAggregateRootCellCache) = {
    val bookingResult =
      if (!deathCertificatesStillDueById.contains(arId)) {
        val bookedCell =
          cellByArId.get(arId) match {
            case Some(cell) =>
              updateLastAccessTimestamp(arId)
              cell
            case None =>
              val newCell = createCell(arId, arType)
              cellByArId += (arId -> newCell)
              uninitializedCells += (arId -> Deadline.now)
              newCell
          }
        val handleId = nextHandleId
        bookingsByArId.get(arId) match {
          case Some(bookings) =>
            bookings += handleId
          case None =>
            bookingsByArId += (arId -> scala.collection.mutable.HashSet(handleId))
        }
        arIdByBookingId += (handleId -> arId)
        val handle = new CellHandle {
          val cell = bookedCell
          def release() = requestUnbook(handleId)
        }
        nextHandleId += 1
        CellBooked(handle)
      } else {
        AwaitingDeathCertificate
      }
    (bookingResult, this)
  }

  def unbookCell(handleId: Long, onStrangeState: String => Unit): MutableAggregateRootCellCache = {
    arIdByBookingId.get(handleId) match {
      case Some(arId) =>
        bookingsByArId.get(arId) match {
          case Some(bookings) =>
            if (bookings.contains(handleId)) {
              updateLastAccessTimestamp(arId)
              bookings -= handleId
              numUnbookings += 1L
            } else {
              onStrangeState(s"""Unbooking of handle id $handleId for AR $arId was requested but the booking was not registered for the AR.""")
            }
            if (bookings.isEmpty)
              bookingsByArId -= arId
          case None =>
            onStrangeState(s"""Unbooking of handle id $handleId for AR $arId was requested but there were no bookings.""")
        }
      case None =>
        onStrangeState(s"""Unbooking of handle id $handleId was requested but there is no AR booked""")
    }
    this
  }

  def updateLastAccessTimestamp(arId: JUUID) {
    val t = Deadline.now
    if (loadedCells.contains(arId)) {
      loadedCells.update(arId, t)
    } else if (uninitializedCells.contains(arId)) {
      uninitializedCells.update(arId, t)
    } else if (doesNotExistCells.contains(arId)) {
      doesNotExistCells.update(arId, t)
    } else if (errorneousCells.contains(arId)) {
      errorneousCells.update(arId, t)
    }
  }

  def updateCellState(arId: JUUID, newCellState: AggregateRootCellState): MutableAggregateRootCellCache = {
    uninitializedCells -= arId
    loadedCells -= arId
    doesNotExistCells -= arId
    errorneousCells -= arId
    newCellState match {
      case CellStateUninitialized => uninitializedCells += arId -> Deadline.now
      case CellStateError(_) => errorneousCells += arId -> Deadline.now
      case CellStateDoesNotExist => doesNotExistCells += arId -> Deadline.now
      case CellStateLoaded => loadedCells += arId -> Deadline.now
    }
    this
  }

  private def getCandidatesByDeadline(from: scala.collection.mutable.HashMap[JUUID, Deadline], deadline: Deadline) = {
    from.filter {
      case (id, timestamp) =>
        timestamp > deadline && !bookingsByArId.contains(id)
    }.map(_._1)
  }

  private def killCandidates(candidates: Iterable[JUUID], from: scala.collection.mutable.HashMap[JUUID, Deadline]) = {
    var myKills = 0L
    candidates.foreach { candidateId =>
      killCell(cellByArId(candidateId))
      val cell = cellByArId(candidateId)
      deathCertificatesStillDueById += candidateId -> cell
      deathCertificatesStillDueByCell += cell -> candidateId
      cellByArId -= candidateId
      from -= candidateId
      myKills += 1L
    }
    this.kills += myKills
    myKills
  }

  private def killExpired(from: scala.collection.mutable.HashMap[JUUID, Deadline], deadLine: Deadline) = {
    val candidates = getCandidatesByDeadline(from, deadLine)
    killCandidates(candidates, from)
  }

  def cleanUp(maxDoesNotExistAge: Option[FiniteDuration], maxInMemoryLifeTime: Option[FiniteDuration], maxUninitializedAge: Option[FiniteDuration]): (MutableAggregateRootCellCache, MutableAggregateRootCellCacheCleanUpTimings) = {
    val t0 = Deadline.now

    var dumpLoaded = 0L
    maxInMemoryLifeTime.foreach { lt =>
      val deadline = t0 - lt
      val candidates = getCandidatesByDeadline(loadedCells, deadline)
      candidates.map(cellByArId.get).flatten.foreach { x => x ! DropCachedAggregateRoot; dumpLoaded += 1L }
      candidates.size
    }

    val t1 = Deadline.now

    val killedDoesNotExist = maxDoesNotExistAge.map { lt =>
      val deadline = t0 - lt
      killExpired(doesNotExistCells, deadline)
    }.getOrElse(0L)

    val t2 = Deadline.now

    val killedInErrorState = maxDoesNotExistAge.map { lt =>
      val deadline = t0 - lt
      killExpired(errorneousCells, deadline)
    }.getOrElse(0L)

    val t3 = Deadline.now

    val killedUninitialized = maxUninitializedAge.map { lt =>
      val deadline = t0 - lt
      killExpired(uninitializedCells, deadline)
    }.getOrElse(0L)

    val t4 = Deadline.now

    (this,
      MutableAggregateRootCellCacheCleanUpTimings(
        tUninitialized = t4 - t3,
        nKilledUnitialized = killedUninitialized,
        tDoesNotExist = t2 - t1,
        nKilledDoesNotExist = killedDoesNotExist,
        tLoaded = t1 - t0,
        nDumpLoaded = dumpLoaded,
        tError = t3 - t2,
        nKilledInErrorState = killedInErrorState,
        tTotal = t4 - t0))
  }

  def confirmDeath(cell: ActorRef, onStrangeState: String => Unit) {
    deathCertificatesStillDueByCell.get(cell) match {
      case Some(arId) =>
        deathCertificatesStillDueById -= arId
        deathCertificatesStillDueByCell -= cell
        confirmedKills += 1L
      case None =>
        onStrangeState(s"""Unexpected death confirmation: "${cell.path.toString()}"""")
    }
  }

  def arIdForUnconfirmedKill(cell: ActorRef): Option[JUUID] =
    deathCertificatesStillDueByCell.get(cell)

  def stats: AggregateRootCellCacheStats =
    AggregateRootCellCacheStats(
      numCells = cellByArId.size,
      numBookings = bookingsByArId.values.flatten.size,
      numBookedArs = bookingsByArId.size,
      numUninitialized = uninitializedCells.size,
      numLoaded = loadedCells.size,
      numDoesNotExist = doesNotExistCells.size,
      numErrorneous = errorneousCells.size,
      numBookingsInLifetime = nextHandleId - 1L,
      numUnbookingsInLifetime = numUnbookings,
      numDeathCertificatesStillDue = deathCertificatesStillDueByCell.size,
      kills = kills,
      confirmedKills = confirmedKills)
}

case class MutableAggregateRootCellCacheCleanUpTimings(
  tUninitialized: FiniteDuration,
  nKilledUnitialized: Long,
  tDoesNotExist: FiniteDuration,
  nKilledDoesNotExist: Long,
  tLoaded: FiniteDuration,
  nDumpLoaded: Long,
  tError: FiniteDuration,
  nKilledInErrorState: Long,
  tTotal: FiniteDuration) {
  def toNiceString() =
    s"""|clean up timings
       	|
    	|tUninitialized = $tUninitialized(killed: $nKilledUnitialized)
    	|tDoesNotExist  = $tDoesNotExist(killed: $nKilledDoesNotExist)
    	|tLoaded        = $tLoaded(dump: $nDumpLoaded)
    	|tError         = $tError(killed: $nKilledInErrorState)
        |tTotal         = $tTotal""".stripMargin
}