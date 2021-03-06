package almhirt.corex.mongo.snapshots

import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.almfuture.all._
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.snapshots.SnapshotMarshaller
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.commands.WriteConcern
import almhirt.reactivemongox._
import scala.concurrent.Await 

object BinarySnapshotRepository {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    rwMode: ReadWriteMode.SupportsReading,
    marshaller: SnapshotMarshaller[Array[Byte]],
    readWarningThreshold: FiniteDuration,
    writeWarningThreshold: FiniteDuration,
    compress: Boolean,
    initializeRetryPolicy: RetryPolicyExt,
    storageRetryPolicy: RetryPolicyExt,
    futuresExecutionContextSelector: ExtendedExecutionContextSelector,
    marshallingExecutionContextSelector: ExtendedExecutionContextSelector)(implicit almhirtContext: AlmhirtContext): Props =
    Props(new BinarySnapshotRepositoryActor(
      db,
      collectionName,
      rwMode,
      marshaller,
      readWarningThreshold,
      writeWarningThreshold,
      compress,
      initializeRetryPolicy,
      storageRetryPolicy,
      futuresExecutionContextSelector,
      marshallingExecutionContextSelector))

  def propsWithDb(
    db: DB with DBMetaCommands,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.snapshots.repository" + configName.map("." + _).getOrElse("")
    (for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      collectionName ← section.v[String]("collection-name")
      writeWarningThreshold ← section.v[FiniteDuration]("write-warn-threshold")
      readWarningThreshold ← section.v[FiniteDuration]("read-warn-threshold")
      initializeRetryPolicy ← section.v[RetryPolicyExt]("initialize-retry-policy")
      storageRetryPolicy ← section.v[RetryPolicyExt]("storage-retry-policy")
      futuresExecutionContextSelector ← section.v[ExtendedExecutionContextSelector]("futures-context")
      marshallingExecutionContextSelector ← section.v[ExtendedExecutionContextSelector]("marshalling-context")
      compress ← section.v[Boolean]("compress")
      rwMode ← section.v[ReadWriteMode.SupportsReading]("read-write-mode")
    } yield propsRaw(
      db,
      collectionName,
      rwMode,
      marshaller,
      readWarningThreshold,
      writeWarningThreshold,
      compress,
      initializeRetryPolicy,
      storageRetryPolicy,
      futuresExecutionContextSelector,
      marshallingExecutionContextSelector)).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure BinarySnapshotRepository @$path.""", cause = Some(p)))
  }

  def propsWithConnection(
    connection: MongoConnection,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.snapshots.repository" + configName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      dbName ← section.v[String]("db-name")
      db ← inTryCatch { Await.result(connection.database(dbName)(ctx.futuresContext), 10.seconds) }
      props ← propsWithDb(
        db,
        marshaller,
        configName)
    } yield props
  }

  def componentFactory(
    connection: MongoConnection,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[ComponentFactory] =
    propsWithConnection(connection, marshaller, configName).map(props ⇒ ComponentFactory(props, almhirt.snapshots.SnapshotRepository.actorname))

}

private[snapshots] class BinarySnapshotRepositoryActor(
    db: DB with DBMetaCommands,
    collectionName: String,
    rwMode: ReadWriteMode.SupportsReading,
    marshaller: SnapshotMarshaller[Array[Byte]],
    readWarningThreshold: FiniteDuration,
    writeWarningThreshold: FiniteDuration,
    compress: Boolean,
    initializeRetryPolicy: RetryPolicyExt,
    storageRetryPolicy: RetryPolicyExt,
    futuresExecutionContextSelector: ExtendedExecutionContextSelector,
    marshallingExecutionContextSelector: ExtendedExecutionContextSelector)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ControllableActor with StatusReportingActor {
  import almhirt.snapshots.SnapshotRepository

  override val componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))

  implicit val futuresContext = selectExecutionContext(futuresExecutionContextSelector)
  val marshallingContext = selectExecutionContext(marshallingExecutionContextSelector)

  private var numSnapshotsReceived = 0L
  private var numSnapshotsReceivedWhileUninitialized = 0L
  private val numSnapshotsStored = new java.util.concurrent.atomic.AtomicLong(0L)
  private val numSnapshotsNotStored = new java.util.concurrent.atomic.AtomicLong(0L)

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def receiveInitializeReadWrite: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport(None)) {
      case Initialize ⇒
        logInfo("Initializing")
        retryFuture(initializeRetryPolicy) {
          for {
            collectionNames ← db.collectionNames
            creationRes ← if (collectionNames.contains(collectionName)) {
              logInfo(s"""Collection "$collectionName" already exists.""")
              Future.successful(false)
            } else {
              logInfo(s"""Collection "$collectionName" does not yet exist. Create.""")
              val collection = db(collectionName)
              collection.create(true)
            }
          } yield creationRes
        }.onComplete(
          prob ⇒ self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob))),
          succ ⇒ { self ! Initialized })

      case Initialized ⇒
        logInfo(s"Initialized.")
        context.become(receiveRunning)

      case InitializeFailed(cause) ⇒
        logError("Initialization failed.")
        reportCriticalFailure(cause)
      //throw cause.toThrowable

      case SnapshotRepository.StoreSnapshot(ar) ⇒
        numSnapshotsReceivedWhileUninitialized = numSnapshotsReceivedWhileUninitialized + 1L
        logWarning("Received storage message StoreSnapshot while initializing")
        sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
        logWarning("Received storage message MarkAggregateRootMortuus while initializing")
        sender() ! SnapshotRepository.MarkAggregateRootMortuusFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.DeleteSnapshot(id) ⇒
        logWarning("Received storage message DeleteSnapshot while initializing")
        sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.FindSnapshot(id) ⇒
        logWarning("Received storage message FindSnapshot while initializing")
        sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
    }
  }

  def receiveInitializeReadOnly: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport(None)) {
      case Initialize ⇒
        logInfo("Initializing")
        retryFuture(initializeRetryPolicy) {
          for {
            collectionNames ← db.collectionNames
            creationRes ← if (collectionNames.contains(collectionName)) {
              logInfo(s"""Collection "$collectionName" already exists.""")
              AlmFuture.successful(())
            } else {
              AlmFuture.failed(UnspecifiedProblem(s"""Collection "$collectionName" does not exist."""))
            }
          } yield creationRes
        }.onComplete(
          prob ⇒ self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob))),
          succ ⇒ { self ! Initialized })

      case Initialized ⇒
        logInfo(s"Initialized.")
        context.become(receiveRunning)

      case InitializeFailed(cause) ⇒
        logError("Initialization failed.")
        reportCriticalFailure(cause)
      //throw cause.toThrowable

      case SnapshotRepository.StoreSnapshot(ar) ⇒
        numSnapshotsReceivedWhileUninitialized = numSnapshotsReceivedWhileUninitialized + 1L
        logWarning("Received storage message StoreSnapshot while initializing")
        sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
        logWarning("Received storage message MarkAggregateRootMortuus while initializing")
        sender() ! SnapshotRepository.MarkAggregateRootMortuusFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.DeleteSnapshot(id) ⇒
        logWarning("Received storage message DeleteSnapshot while initializing")
        sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

      case SnapshotRepository.FindSnapshot(id) ⇒
        logWarning("Received storage message FindSnapshot while initializing")
        sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
    }
  }

  def receiveRunning: Receive = running() {
    reportsStatusF(onReportRequested = createStatusReport(Some(db.collection(collectionName).count().map(_.toLong)))) {
      case SnapshotRepository.StoreSnapshot(ar) ⇒
        numSnapshotsReceived = numSnapshotsReceived + 1L
        val f = measureWrite {
          for {
            snapshot ← marshal(ar)
            storedAggId ← storeSnapshot(snapshot)
          } yield {
            numSnapshotsStored.incrementAndGet()
            SnapshotRepository.SnapshotStored(storedAggId)
          }
        }
        f.recoverThenPipeTo(fail ⇒ {
          numSnapshotsNotStored.incrementAndGet()
          SnapshotRepository.StoreSnapshotFailed(ar.id, fail)
        })(sender())

      case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
        val f = measureWrite { markSnapshotMortuus(PersistableMortuusSnapshotState(id, version)).map(SnapshotRepository.AggregateRootMarkedMortuus(_)) }
        f.recoverThenPipeTo(fail ⇒ SnapshotRepository.MarkAggregateRootMortuusFailed(id, fail))(sender())

      case SnapshotRepository.DeleteSnapshot(id) ⇒
        val f = measureWrite { deleteSnapshot(id).map(SnapshotRepository.SnapshotDeleted(_)) }
        f.recoverThenPipeTo(fail ⇒ SnapshotRepository.DeleteSnapshotFailed(id, fail))(sender())

      case SnapshotRepository.FindSnapshot(id) ⇒
        val f = measureRead { findSnapshot(id) }
        f.recoverThenPipeTo(fail ⇒ SnapshotRepository.FindSnapshotFailed(id, fail))(sender())
    }
  }

  override def receive: Receive = Actor.emptyBehavior

  private def measureRead[T](f: ⇒ AlmFuture[T]): AlmFuture[T] = {
    val start = Deadline.now
    f.onComplete { res ⇒
      val lap = start.lap
      if (lap > readWarningThreshold)
        logWarning(s"Read operationen took longer than ${readWarningThreshold.defaultUnitString}(${lap.defaultUnitString}).")
    }
  }

  private def measureWrite[T](f: ⇒ AlmFuture[T]): AlmFuture[T] = {
    val start = Deadline.now
    f.onComplete { res ⇒
      val lap = start.lap
      if (lap > writeWarningThreshold)
        logWarning(s"Write operationen took longer than ${writeWarningThreshold.defaultUnitString}(${lap.defaultUnitString}).")
    }
  }

  private def marshal(ar: almhirt.aggregates.AggregateRoot): AlmFuture[BinarySnapshotState] =
    if (compress) {
      import org.xerial.snappy.Snappy
      AlmFuture(marshaller.marshal(ar).map(bytes ⇒ PersistableSnappyCompressedVivusSnapshotState(ar.id, ar.version, Snappy.compress(bytes))))(marshallingContext)
    } else {
      AlmFuture(marshaller.marshal(ar).map(PersistableBinaryVivusSnapshotState(ar.id, ar.version, _)))(marshallingContext)
    }

  private def storeSnapshot(snapshot: PersistableSnapshotState): AlmFuture[AggregateRootId] = {
    rwMode.useForWriteOp { writeConcern ⇒
      val collection = db(collectionName)
      retryFuture(storageRetryPolicy)(
        collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: PersistableSnapshotState, writeConcern = writeConcern, upsert = true, multi = false).toAlmFuture.mapV(res ⇒
          if (res.ok) {
            scalaz.Success(snapshot.aggId)
          } else {
            val prob = PersistenceProblem(s"""Failed to upsert snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value}: ${res.errmsg}""")
            reportMajorFailure(prob)
            scalaz.Failure(prob)
          }))
    }
  }

  private def markSnapshotMortuus(snapshot: PersistableMortuusSnapshotState): AlmFuture[AggregateRootId] = {
    rwMode.useForWriteOp { writeConcern ⇒
      val collection = db(collectionName)
      retryFuture(storageRetryPolicy)(
        collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: PersistableSnapshotState, writeConcern = writeConcern, upsert = true, multi = false).toAlmFuture.mapV(res ⇒
          if (res.ok) {
            scalaz.Success(snapshot.aggId)
          } else {
            val prob = PersistenceProblem(s"""Failed to mark snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value} as mortuus: ${res.errmsg}""")
            reportMajorFailure(prob)
            scalaz.Failure(prob)
          }))
    }
  }

  private def deleteSnapshot(id: AggregateRootId): AlmFuture[AggregateRootId] = {
    rwMode.useForWriteOp { writeConcern ⇒
      val collection = db(collectionName)
      retryFuture(storageRetryPolicy)(
        collection.remove(BSONDocument("_id" -> id.value), writeConcern = writeConcern, firstMatchOnly = true).toAlmFuture.mapV(res ⇒
          if (res.ok) {
            scalaz.Success(id)
          } else {
            val prob = PersistenceProblem(s"""Failed to delete snapshot for ${id.value}: Error Code ${res.code}""")
            reportMajorFailure(prob)
            scalaz.Failure(prob)
          }))
    }
  }

  private def findSnapshot(id: AggregateRootId): AlmFuture[SnapshotRepository.FindSnapshotResponse] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      (for {
        doc ← collection.find(BSONDocument("_id" -> id.value)).cursor[PersistableSnapshotState](readPreference = rwMode.readPreference).headOption
        rsp ← doc match {
          case Some(PersistableBinaryVivusSnapshotState(_, _, bin)) ⇒
            AlmFuture(marshaller.unmarshal(bin).map(SnapshotRepository.FoundSnapshot(_)))(marshallingContext)
          case Some(PersistableSnappyCompressedVivusSnapshotState(_, _, snappyData)) ⇒
            import org.xerial.snappy.Snappy
            AlmFuture(marshaller.unmarshal(Snappy.uncompress(snappyData)).map(SnapshotRepository.FoundSnapshot(_)))(marshallingContext)
          case Some(PersistableBsonVivusSnapshotState(_, _, _)) ⇒
            val prob = UnspecifiedProblem("This storage does not support a BSON representation of an aggregate root.")
            reportMajorFailure(prob)
            AlmFuture.successful(SnapshotRepository.FindSnapshotFailed(id, prob))
          case Some(PersistableMortuusSnapshotState(id, version)) ⇒
            AlmFuture.successful(SnapshotRepository.AggregateRootWasDeleted(id, version))
          case None ⇒
            AlmFuture.successful(SnapshotRepository.SnapshotNotFound(id))
        }
      } yield rsp))
  }

  private def createStatusReport(numSnapshots: Option[AlmFuture[Long]])(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val baseRep = StatusReport("SnapshotRepository-Report").withComponentState(componentState) addMany (
      "number-of-snapshots-received" -> numSnapshotsReceived,
      "number-of-snapshots-received-while-uninitialized" -> numSnapshotsReceivedWhileUninitialized,
      "number-of-snapshots-stored" -> numSnapshotsNotStored.get,
      "number-of-snapshots-not-stored" -> numSnapshotsNotStored.get) subReport ("config",
        "collection-name" -> collectionName,
        "compress" -> (if (compress) "snappy" else "no"))

    for {
      totalSnapshots ← numSnapshots match {
        case None      ⇒ AlmFuture.successful(None)
        case Some(fut) ⇒ fut.materializedValidation.map(Some(_))
      }
    } yield baseRep ~ ("snapshots-persisted" -> totalSnapshots)
  }

  override def preStart() {
    if (rwMode.supportsWriting) {
      logInfo("Starting(r/w)...")
      context.become(receiveInitializeReadWrite)
    } else {
      logInfo("Starting(ro)...")
      context.become(receiveInitializeReadOnly)
    }
    logInfo(s"collection: ${collectionName}\nWrite warn after ${readWarningThreshold.defaultUnitString}\nRead warn after ${writeWarningThreshold.defaultUnitString}\nCompress: $compress\nReadWriteMode: $rwMode")
    registerComponentControl()
    registerStatusReporter(description = Some("SnapshotRepository based on MongoDB"))
    self ! Initialize
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
    logWarning("Stopped")
  }

}