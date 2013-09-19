package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
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
    	|confirmedKills               = $confirmedKills
    	|""".stripMargin
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

  def toNiceDiffStringWith(other: AggregateRootCellCacheStats, msg: String = "difference"): String = {
    val diff = this - other
    s"""|Aggregate root cell cache statistics(difference)
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
    	|confirmedKills               = $confirmedKills - ${other.confirmedKills} = ${diff.confirmedKills}
    	|""".stripMargin
  }
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

  private val uninitializedCells = scala.collection.mutable.HashMap.empty[JUUID, Long]
  private val loadedCells = scala.collection.mutable.HashMap.empty[JUUID, Long]
  private val doesNotExistCells = scala.collection.mutable.HashMap.empty[JUUID, Long]
  private val errorneousCells = scala.collection.mutable.HashMap.empty[JUUID, Long]
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
              cell
            case None =>
              val newCell = createCell(arId, arType)
              cellByArId += (arId -> newCell)
              uninitializedCells += (arId -> System.currentTimeMillis())
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

  def updateCellState(arId: JUUID, newCellState: AggregateRootCellState): MutableAggregateRootCellCache = {
    uninitializedCells -= arId
    loadedCells -= arId
    doesNotExistCells -= arId
    errorneousCells -= arId
    newCellState match {
      case CellStateUninitialized => uninitializedCells += arId -> System.currentTimeMillis()
      case CellStateError(_) => errorneousCells += arId -> System.currentTimeMillis()
      case CellStateDoesNotExist => doesNotExistCells += arId -> System.currentTimeMillis()
      case CellStateLoaded => loadedCells += arId -> System.currentTimeMillis()
    }
    this
  }

  private def getCandidatesByDeadline(from: scala.collection.mutable.HashMap[JUUID, Long], deadLine: Long) = {
    from.filterNot {
      case (id, timestamp) =>
        bookingsByArId.contains(id) || timestamp < deadLine
    }.map(_._1)
  }

  private def killCandidates(candidates: Iterable[JUUID], from: scala.collection.mutable.HashMap[JUUID, Long]) {
    candidates.foreach { candidateId =>
      killCell(cellByArId(candidateId))
      val cell = cellByArId(candidateId)
      deathCertificatesStillDueById += candidateId -> cell
      deathCertificatesStillDueByCell += cell -> candidateId
      cellByArId -= candidateId
      from -= candidateId
      kills += 1L
    }
  }

  private def killExpired(from: scala.collection.mutable.HashMap[JUUID, Long], deadLine: Long) {
    val candidates = getCandidatesByDeadline(from, deadLine)
    killCandidates(candidates, from)
  }

  def cleanUp(maxDoesNotExistAge: Option[FiniteDuration], maxInMemoryLifeTime: Option[FiniteDuration], maxUninitializedAge: Option[FiniteDuration]): (MutableAggregateRootCellCache, FiniteDuration) = {
    val start = System.currentTimeMillis()

    maxInMemoryLifeTime.foreach { lt =>
      val deadline = System.currentTimeMillis() - lt.toMillis
      val candidates = getCandidatesByDeadline(loadedCells, deadline)
      candidates.map(cellByArId.get).flatten.foreach(_ ! DropCachedAggregateRoot)
    }

    maxDoesNotExistAge.foreach { lt =>
      val deadline = System.currentTimeMillis() - lt.toMillis
      killExpired(doesNotExistCells, deadline)
      killExpired(errorneousCells, deadline)
    }

    maxUninitializedAge.foreach { lt =>
      val deadline = System.currentTimeMillis() - lt.toMillis
      killExpired(uninitializedCells, deadline)
    }

    val time = FiniteDuration(System.currentTimeMillis() - start, "ms")
    (this, time)
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