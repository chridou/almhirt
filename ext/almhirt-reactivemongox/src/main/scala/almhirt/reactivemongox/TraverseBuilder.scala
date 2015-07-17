package almhirt.reactivemongox

import scala.concurrent.ExecutionContext
import reactivemongo.bson.BSONDocument
import almhirt.common.TraverseWindow
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocumentReader
import play.api.libs.iteratee.Enumerator

trait TraverseBuilder {
  def collection: BSONCollection

  def readPreference: ReadPreferenceAlm
  def stopOnError: Boolean
  def selector: Option[BSONDocument]
  def projection: Option[BSONDocument]
  def sort: Option[BSONDocument]
  def skip: TraverseWindow.LowerBound
  def take: TraverseWindow.Length

  final def traverseWindow: TraverseWindow = TraverseWindow(skip, take)

  def withStopOnError: TraverseBuilder
  def noStopOnError: TraverseBuilder
  def withSelector(v: BSONDocument): TraverseBuilder
  def withProjection(v: BSONDocument): TraverseBuilder
  def withSort(v: BSONDocument): TraverseBuilder
  def withTake(v: Int): TraverseBuilder
  def withSkip(v: Int): TraverseBuilder
  def withWindow(v: TraverseWindow): TraverseBuilder
  def takeAll: TraverseBuilder
  def skipNone: TraverseBuilder

  final def enumerate[T: BSONDocumentReader](implicit ctx: ExecutionContext): Enumerator[T] =
    collection.traverseWith[T](selector, projection, sort, traverseWindow, readPreference, stopOnError)
}

object TraverseBuilder {
  def apply(collection: BSONCollection, readPreference: ReadPreferenceAlm): TraverseBuilder = new TraverseBuilderImpl(
    collection = collection,
    readPreference = readPreference)

  def apply(collection: BSONCollection, selector: BSONDocument, readPreference: ReadPreferenceAlm): TraverseBuilder = new TraverseBuilderImpl(
    collection = collection,
    readPreference = readPreference,
    selector = Some(selector))

  def apply(collection: BSONCollection, selector: BSONDocument, projection: BSONDocument, readPreference: ReadPreferenceAlm): TraverseBuilder = new TraverseBuilderImpl(
    collection = collection,
    readPreference = readPreference,
    projection = Some(projection),
    selector = Some(selector))

  def apply(collection: BSONCollection, selector: BSONDocument, projection: BSONDocument, sort: BSONDocument, readPreference: ReadPreferenceAlm): TraverseBuilder = new TraverseBuilderImpl(
    collection = collection,
    readPreference = readPreference,
    projection = Some(projection),
    sort = Some(sort),
    selector = Some(selector))

  private final case class TraverseBuilderImpl(
      collection: BSONCollection,
      readPreference: ReadPreferenceAlm,
      stopOnError: Boolean = true,
      selector: Option[BSONDocument] = None,
      projection: Option[BSONDocument] = None,
      sort: Option[BSONDocument] = None,
      skip: TraverseWindow.LowerBound = TraverseWindow.SkipNone,
      take: TraverseWindow.Length = TraverseWindow.TakeAll) extends TraverseBuilder {

    override def withStopOnError: TraverseBuilder = this.copy(stopOnError = true)
    override def noStopOnError: TraverseBuilder = this.copy(stopOnError = false)
    override def withSelector(v: BSONDocument): TraverseBuilder = this.copy(selector = Some(v))
    override def withProjection(v: BSONDocument): TraverseBuilder = this.copy(projection = Some(v))
    override def withSort(v: BSONDocument): TraverseBuilder = this.copy(sort = Some(v))
    override def withTake(v: Int): TraverseBuilder = this.copy(take = TraverseWindow.Take(v))
    override def withSkip(v: Int): TraverseBuilder = this.copy(skip = TraverseWindow.Skip(v))
    override def takeAll: TraverseBuilder = this.copy(take = TraverseWindow.TakeAll)
    override def skipNone: TraverseBuilder = this.copy(skip = TraverseWindow.SkipNone)
    override def withWindow(v: TraverseWindow): TraverseBuilder = this.copy(take = v.take, skip = v.skip)
  }
}