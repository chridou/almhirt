package almhirt.corex.mongo.snapshots

import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.almfuture.all._
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.snapshots.SnapshotMarshaller
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.GetLastError

object BinarySnapshotRepository {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    getLastError: GetLastError,
    marshaller: SnapshotMarshaller[Array[Byte]],
    readWarningThreshold: FiniteDuration,
    writeWarningThreshold: FiniteDuration,
    readOnly: Boolean,
    compress: Boolean,
    initializeRetryPolicy: RetryPolicyExt,
    storageRetryPolicy: RetryPolicyExt,
    circuitControlSettings: CircuitControlSettings,
    futuresExecutionContextSelector: ExtendedExecutionContextSelector,
    marshallingExecutionContextSelector: ExtendedExecutionContextSelector)(implicit almhirtContext: AlmhirtContext): Props =
    Props(new BinarySnapshotRepositoryActor(
      db,
      collectionName,
      getLastError,
      marshaller,
      readWarningThreshold,
      writeWarningThreshold,
      readOnly,
      compress,
      initializeRetryPolicy,
      storageRetryPolicy,
      circuitControlSettings,
      futuresExecutionContextSelector,
      marshallingExecutionContextSelector))

  def propsWithDb(
    db: DB with DBMetaCommands,
    getLastError: GetLastError,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.snapshots.repository" + configName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      collectionName ← section.v[String]("collection-name")
      writeWarningThreshold ← section.v[FiniteDuration]("write-warn-threshold")
      readWarningThreshold ← section.v[FiniteDuration]("read-warn-threshold")
      circuitControlSettings ← section.v[CircuitControlSettings]("circuit-control")
      initializeRetryPolicy ← section.v[RetryPolicyExt]("initialize-retry-policy")
      storageRetryPolicy ← section.v[RetryPolicyExt]("storage-retry-policy")
      futuresExecutionContextSelector ← section.v[ExtendedExecutionContextSelector]("futures-context")
      marshallingExecutionContextSelector ← section.v[ExtendedExecutionContextSelector]("marshalling-context")
      readOnly ← section.v[Boolean]("read-only")
      compress ← section.v[Boolean]("compress")
    } yield propsRaw(
      db,
      collectionName,
      getLastError,
      marshaller,
      readWarningThreshold,
      writeWarningThreshold,
      readOnly,
      compress,
      initializeRetryPolicy,
      storageRetryPolicy,
      circuitControlSettings,
      futuresExecutionContextSelector,
      marshallingExecutionContextSelector)
  }

  def propsWithConnection(
    connection: MongoConnection,
    getLastError: GetLastError,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.snapshots.repository" + configName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      dbName ← section.v[String]("db-name")
      db ← inTryCatch { connection(dbName)(ctx.futuresContext) }
      props ← propsWithDb(
        db,
        getLastError,
        marshaller,
        configName)
    } yield props
  }

  def componentFactory(
    connection: MongoConnection,
    getLastError: GetLastError,
    marshaller: SnapshotMarshaller[Array[Byte]],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[ComponentFactory] =
    propsWithConnection(connection, getLastError, marshaller, configName).map(props ⇒ ComponentFactory(props, almhirt.snapshots.SnapshotRepository.actorname))

}

private[snapshots] class BinarySnapshotRepositoryActor(
    db: DB with DBMetaCommands,
    collectionName: String,
    getLastError: GetLastError,
    marshaller: SnapshotMarshaller[Array[Byte]],
    readWarningThreshold: FiniteDuration,
    writeWarningThreshold: FiniteDuration,
    readOnly: Boolean,
    compress: Boolean,
    initializeRetryPolicy: RetryPolicyExt,
    storageRetryPolicy: RetryPolicyExt,
    circuitControlSettings: CircuitControlSettings,
    futuresExecutionContextSelector: ExtendedExecutionContextSelector,
    marshallingExecutionContextSelector: ExtendedExecutionContextSelector)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
  import almhirt.snapshots.SnapshotRepository

  implicit val futuresContext = selectExecutionContext(futuresExecutionContextSelector)
  val marshallingContext = selectExecutionContext(marshallingExecutionContextSelector)

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, futuresContext, context.system.scheduler)

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def receiveInitializeReadWrite: Receive = {
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
      registerCircuitControl(circuitBreaker)
      context.become(receiveRunning)

    case InitializeFailed(cause) ⇒
      logError("Initialization failed.")
      reportCriticalFailure(cause)
    //throw cause.toThrowable

    case SnapshotRepository.StoreSnapshot(ar) ⇒
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

  def receiveInitializeReadOnly: Receive = {
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
      registerCircuitControl(circuitBreaker)
      context.become(receiveRunning)

    case InitializeFailed(cause) ⇒
      logError("Initialization failed.")
      reportCriticalFailure(cause)
    //throw cause.toThrowable

    case SnapshotRepository.StoreSnapshot(ar) ⇒
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

  def receiveRunning: Receive = {

    case SnapshotRepository.StoreSnapshot(ar) ⇒
      val f = measureWrite(for {
        snapshot ← marshal(ar)
        storedAggId ← circuitBreaker.fused(storeSnapshot(snapshot))
      } yield SnapshotRepository.SnapshotStored(storedAggId))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.StoreSnapshotFailed(ar.id, fail))(sender())

    case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
      val f = measureWrite(circuitBreaker.fused(markSnapshotMortuus(PersistableMortuusSnapshotState(id, version))).map(SnapshotRepository.AggregateRootMarkedMortuus(_)))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.MarkAggregateRootMortuusFailed(id, fail))(sender())

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      val f = measureWrite(circuitBreaker.fused(deleteSnapshot(id)).map(SnapshotRepository.SnapshotDeleted(_)))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.DeleteSnapshotFailed(id, fail))(sender())

    case SnapshotRepository.FindSnapshot(id) ⇒
      val f = measureRead(circuitBreaker.fused(findSnapshot(id)))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.FindSnapshotFailed(id, fail))(sender())
  }

  override def receive: Receive = Actor.emptyBehavior

  private def measureRead[T](f: => AlmFuture[T]): AlmFuture[T] = {
    val start = Deadline.now
    f.onComplete { res ⇒
      val lap = start.lap
      if (lap > readWarningThreshold)
        logWarning(s"Read operationen took longer than ${readWarningThreshold.defaultUnitString}(${lap.defaultUnitString}).")
    }
  }

  private def measureWrite[T](f: => AlmFuture[T]): AlmFuture[T] = {
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
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: PersistableSnapshotState, writeConcern = getLastError, upsert = true, multi = false).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(snapshot.aggId)
        } else {
          val prob = PersistenceProblem(s"""Failed to upsert snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value}: ${res.message}""")
          reportMajorFailure(prob)
          scalaz.Failure(prob)
        }))
  }

  private def markSnapshotMortuus(snapshot: PersistableMortuusSnapshotState): AlmFuture[AggregateRootId] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: PersistableSnapshotState, writeConcern = getLastError, upsert = true, multi = false).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(snapshot.aggId)
        } else {
          val prob = PersistenceProblem(s"""Failed to mark snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value} as mortuus: ${res.message}""")
          reportMajorFailure(prob)
          scalaz.Failure(prob)
        }))
  }

  private def deleteSnapshot(id: AggregateRootId): AlmFuture[AggregateRootId] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.remove(BSONDocument("_id" -> id.value), writeConcern = getLastError, firstMatchOnly = true).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(id)
        } else {
          val prob = PersistenceProblem(s"""Failed to delete snapshot for ${id.value}: ${res.message}""")
          reportMajorFailure(prob)
          scalaz.Failure(prob)
        }))
  }

  private def findSnapshot(id: AggregateRootId): AlmFuture[SnapshotRepository.FindSnapshotResponse] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      (for {
        docs ← collection.find(BSONDocument("_id" -> id.value)).cursor[PersistableSnapshotState].collect[List](2, true).toAlmFuture
        snapshotFromStorage ← AlmFuture {
          docs match {
            case Nil ⇒
              scalaz.Success(None)
            case (x: PersistableSnapshotState) :: Nil ⇒
              scalaz.Success(Some(x))
            case x ⇒
              val prob = PersistenceProblem(s"""Expected 0..1 snapshots of type PersistableSnapshotState with id "${id.value}". Found ${x.size}.""")
              reportMajorFailure(prob)
              scalaz.Failure(prob)
          }
        }
        rsp ← snapshotFromStorage match {
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

  override def preStart() {
    if (readOnly) {
      logInfo("Starting(ro)...")
      context.become(receiveInitializeReadOnly)
    } else {
      logInfo("Starting(r/w)...")
      context.become(receiveInitializeReadWrite)
    }
    logInfo(s"Write warn after ${readWarningThreshold.defaultUnitString}\nRead warn after ${writeWarningThreshold.defaultUnitString}\nCompress: $compress")
    self ! Initialize
  }

}