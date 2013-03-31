package almhirt.domain.components

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation
import scalaz.std._
import org.joda.time.DateTime
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.almakka.AlmActorLogging
import almhirt.domain.components._

class SyncSnapshotStorageActor(storage: SyncSnapshotStorage, theAlmhirt: Almhirt) extends Actor with CanLogProblems with AlmActorLogging {
  def run(what: => Unit) = theAlmhirt.cruncher.execute(what)
  override def receive: Receive = {
    case ev: SnapshotStorageReq =>
      ev match {
        case GetSnapshotQry(id) =>
          val pinnedSender = sender
          run {
            storage.getSnapshot(id).onResult(
              problem => {
                theAlmhirt.publishProblem(problem)
                pinnedSender ! SnapshotRsp(None, id)
              },
              ar => sender ! SnapshotRsp(Some(ar), id))
          }
        case PutSnapshotCmd(ar) =>
          run {
            storage.putSnapshot(ar).onFailure(problem => theAlmhirt.publishProblem(problem))
          }
        case ContainsSnapshotQry(id) =>
          sender ! ContainsSnapshotRsp(false, id)
          val pinnedSender = sender
          run {
            storage.containsSnapshot(id).onResult(
              problem => {
                theAlmhirt.publishProblem(problem)
                pinnedSender ! ContainsSnapshotRsp(false, id)
              },
              isContained => sender ! ContainsSnapshotRsp(isContained, id))
          }
        case GetVersionForSnapshotQry(id) =>
          sender ! ContainsSnapshotRsp(false, id)
          val pinnedSender = sender
          run {
            storage.getVersionForSnapshot(id).onResult(
              problem => {
                theAlmhirt.publishProblem(problem)
                pinnedSender ! VersionForSnapshotRsp(None, id)
              },
              version => sender ! VersionForSnapshotRsp(Some(version), id))
          }
      }
  }

}