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
  numUnbookingsInLifetime: Long) {
  def toNiceString: String = {
    s"""|AggregateRootCellCacheStats(
    	|  numCells = $numCells
    	|  numBookings = $numBookings
    	|  numBookedArs = $numBookedArs
    	|  numUninitialized = $numUninitialized
    	|  numLoaded = $numLoaded
    	|  numDoesNotExist = $numDoesNotExist
    	|  numErrorneous = $numErrorneous
    	|  numBookingsInLifetime = $numBookingsInLifetime
    	|  numUnbookingsInLifetime = $numUnbookingsInLifetime
    	|  bookingDiff = ${numBookingsInLifetime - numUnbookingsInLifetime}
    	|)""".stripMargin
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
      numUnbookingsInLifetime - other.numUnbookingsInLifetime)
      
  def toNiceDiffString(other: AggregateRootCellCacheStats): String = {
    val diff = this - other
    s"""|AggregateRootCellCacheStats(
    	|  numCells = $numCells - ${other.numCells} = ${diff.numCells}
        |  numBookings = $numBookings - ${other.numBookings} = ${diff.numBookings}
       	|  numBookedArs = $numBookedArs - ${other.numBookedArs} = ${diff.numBookedArs}
    	|  numUninitialized = $numUninitialized - ${other.numUninitialized} = ${diff.numUninitialized}
    	|  numLoaded = $numLoaded - ${other.numLoaded} = ${diff.numLoaded}
    	|  numDoesNotExist = $numDoesNotExist - ${other.numDoesNotExist} = ${diff.numDoesNotExist}
    	|  numErrorneous = $numErrorneous - ${other.numErrorneous} = ${diff.numErrorneous}
    	|  numBookingsInLifetime = $numBookingsInLifetime - ${other.numBookingsInLifetime} = ${diff.numBookingsInLifetime} 
    	|  numUnbookingsInLifetime = $numUnbookingsInLifetime - ${other.numUnbookingsInLifetime} = ${diff.numUnbookingsInLifetime}
    	|  bookingDiff = ${numBookingsInLifetime - numUnbookingsInLifetime } - ${other.numBookingsInLifetime - other.numUnbookingsInLifetime } = ${diff.numBookingsInLifetime - diff.numUnbookingsInLifetime }
    	|)""".stripMargin
  }
}

class MutableAggregateRootCellCache(
  createCell: (JUUID, Class[_]) => ActorRef,
  killCell: ActorRef => Unit,
  requestUnbook: Long => Unit) {
  private val cellByArId = scala.collection.mutable.HashMap.empty[JUUID, ActorRef]
  private val bookingsByArId = scala.collection.mutable.HashMap.empty[JUUID, scala.collection.mutable.HashSet[Long]]
  private val arIdByBookingId = scala.collection.mutable.HashMap.empty[Long, JUUID]

  private val uninitializedCells = scala.collection.mutable.HashSet.empty[JUUID]
  private val loadedCells = scala.collection.mutable.HashSet.empty[JUUID]
  private val doesNotExistCells = scala.collection.mutable.HashMap.empty[JUUID, Long]
  private val errorneousCells = scala.collection.mutable.HashSet.empty[JUUID]
  private var nextHandleId = 1L
  private var numUnbookings = 0L

  def bookCell(arId: JUUID, arType: Class[_], onStrangeState: String => Unit): (CellHandle, MutableAggregateRootCellCache) = {
    val bookedCell =
      cellByArId.get(arId) match {
        case Some(cell) =>
          cell
        case None =>
          val newCell = createCell(arId, arType)
          cellByArId += (arId -> newCell)
          uninitializedCells += arId
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
    (handle, this)
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
      case CellStateUninitialized => uninitializedCells += arId
      case CellStateError(_) => errorneousCells += arId
      case CellStateDoesNotExist => doesNotExistCells += arId -> System.currentTimeMillis()
      case CellStateLoaded => loadedCells += arId
    }
    this
  }

  def cleanUp(maxDoesNotExistAge: Option[FiniteDuration], maxInMemoryLifeTime: Option[FiniteDuration]): (MutableAggregateRootCellCache, FiniteDuration) = {
    val start = System.currentTimeMillis()
    maxInMemoryLifeTime.foreach(lt =>
      loadedCells.map(cellByArId.get).flatten.foreach(_ ! ClearCachedOlderThan(new org.joda.time.Duration(lt.toMillis))))
    val effMaxDoesNotExistMillis = maxDoesNotExistAge.map(_.toMillis).getOrElse(0L)
    val maxDoesNotExistDeadLine = System.currentTimeMillis() - effMaxDoesNotExistMillis
    val candidatesToRemove =
      doesNotExistCells.filterNot {
        case (id, timestamp) =>
          bookingsByArId.contains(id) || timestamp < maxDoesNotExistDeadLine
      }.map(_._1)
    candidatesToRemove.foreach { candidateId =>
      killCell(cellByArId(candidateId))
      cellByArId -= candidateId
      doesNotExistCells -= candidateId
    }
    val time = FiniteDuration(start - System.currentTimeMillis(), "ms")
    (this, time)
  }

  def stats: AggregateRootCellCacheStats =
    AggregateRootCellCacheStats(
      cellByArId.size,
      bookingsByArId.values.flatten.size,
      bookingsByArId.size,
      uninitializedCells.size,
      loadedCells.size,
      doesNotExistCells.size,
      errorneousCells.size,
      nextHandleId - 1L,
      numUnbookings)
}