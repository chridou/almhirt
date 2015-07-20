package almhirt.reactivemongox

import scala.concurrent.ExecutionContext
import reactivemongo.bson.BSONDocument
import almhirt.common.TraverseWindow
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocumentReader
import play.api.libs.iteratee.Enumerator

trait QueryBuilderAlm {
  def collection: BSONCollection

  def currentReadPreference: ReadPreferenceAlm
  def currentStopOnError: Boolean
  def currentSelector: Option[BSONDocument]
  def currentProjection: Option[BSONDocument]
  def currentSort: Option[BSONDocument]
  final def currentSkip: TraverseWindow.LowerBound = currentTraverseWindow.skip
  final def currentTake: TraverseWindow.Length = currentTraverseWindow.take

  def currentTraverseWindow: TraverseWindow

  def readPreference(rp: ReadPreferenceAlm): QueryBuilderAlm
  def stopOnError: QueryBuilderAlm
  def noStopOnError: QueryBuilderAlm
  def selector(v: BSONDocument): QueryBuilderAlm
  def project(v: BSONDocument): QueryBuilderAlm
  def sort(v: BSONDocument): QueryBuilderAlm
  def take(v: Int): QueryBuilderAlm
  def skip(v: Int): QueryBuilderAlm
  def traverse(v: TraverseWindow): QueryBuilderAlm
  def takeAll: QueryBuilderAlm
  def skipNone: QueryBuilderAlm

  final def enumerate[T: BSONDocumentReader](implicit ctx: ExecutionContext): Enumerator[T] =
    collection.traverseWith[T](currentSelector, currentProjection, currentSort, currentTraverseWindow, currentReadPreference, currentStopOnError)
}

object QueryBuilderAlm {
  def apply(collection: BSONCollection): QueryBuilderAlm = new QueryBuilderAlmImpl(
    collection = collection)

  def apply(collection: BSONCollection, readPreference: ReadPreferenceAlm): QueryBuilderAlm = new QueryBuilderAlmImpl(
    collection = collection,
    currentReadPreference = readPreference)

  def apply(collection: BSONCollection, selector: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = new QueryBuilderAlmImpl(
    collection = collection,
    currentReadPreference = readPreference,
    currentSelector = Some(selector))

  def apply(collection: BSONCollection, selector: BSONDocument, projection: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = new QueryBuilderAlmImpl(
    collection = collection,
    currentReadPreference = readPreference,
    currentProjection = Some(projection),
    currentSelector = Some(selector))

  def apply(collection: BSONCollection, selector: BSONDocument, projection: BSONDocument, sort: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = new QueryBuilderAlmImpl(
    collection = collection,
    currentReadPreference = readPreference,
    currentProjection = Some(projection),
    currentSort = Some(sort),
    currentSelector = Some(selector))

  private final case class QueryBuilderAlmImpl(
      collection: BSONCollection,
      currentReadPreference: ReadPreferenceAlm = ReadPreferenceAlm.PrimaryPreferred(),
      currentStopOnError: Boolean = true,
      currentSelector: Option[BSONDocument] = None,
      currentProjection: Option[BSONDocument] = None,
      currentSort: Option[BSONDocument] = None,
      currentTraverseWindow: TraverseWindow = TraverseWindow(TraverseWindow.SkipNone, TraverseWindow.TakeAll)) extends QueryBuilderAlm {

    override def readPreference(rp: ReadPreferenceAlm): QueryBuilderAlm = this.copy(currentReadPreference = rp)
    override def stopOnError: QueryBuilderAlm = this.copy(currentStopOnError = true)
    override def noStopOnError: QueryBuilderAlm = this.copy(currentStopOnError = false)
    override def selector(v: BSONDocument): QueryBuilderAlm = this.copy(currentSelector = Some(v))
    override def project(v: BSONDocument): QueryBuilderAlm = this.copy(currentProjection = Some(v))
    override def sort(v: BSONDocument): QueryBuilderAlm = this.copy(currentSort = Some(v))
    override def take(v: Int): QueryBuilderAlm = this.copy(currentTraverseWindow = this.currentTraverseWindow.setLength(TraverseWindow.Take(v)))
    override def skip(v: Int): QueryBuilderAlm = this.copy(currentTraverseWindow = this.currentTraverseWindow.setSkip(TraverseWindow.Skip(v)))
    override def takeAll: QueryBuilderAlm = this.copy(currentTraverseWindow = this.currentTraverseWindow.setLength(TraverseWindow.TakeAll))
    override def skipNone: QueryBuilderAlm = this.copy(currentTraverseWindow = this.currentTraverseWindow.setSkip(TraverseWindow.SkipNone))
    override def traverse(v: TraverseWindow): QueryBuilderAlm = this.copy(currentTraverseWindow = v)
  }
}