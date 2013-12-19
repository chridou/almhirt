package almhirt.http

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicReference

trait AlmMediaTypesRegistry extends Iterable[AlmMediaType] {
  private val byMainAndSubTypes = new AtomicReference(Map.empty[String, Map[String, AlmMediaType]])
  private val byMainTypeAndContent = new AtomicReference(Map.empty[String, Map[String, List[AlmMediaType]]])

  def register(mediaType: AlmMediaType) {
    @tailrec def registerByMainAndSubTypeType(): Unit = {
      val current = byMainAndSubTypes.get()
      val updated = current.get(mediaType.mainType) match {
        case Some(bySubType) => current.updated(mediaType.mainType, bySubType.updated(mediaType.subTypeValue, mediaType))
        case None => current.updated(mediaType.mainType, Map(mediaType.subTypeValue -> mediaType))
      }
      if (!byMainAndSubTypes.compareAndSet(current, updated)) registerByMainAndSubTypeType()
    }

    @tailrec def registerByMainTypeAndContent(): Unit = {
      val current = byMainTypeAndContent.get()
      val updated = current.get(mediaType.mainType) match {
        case Some(byContentValue) =>
          byContentValue.get(mediaType.contentValue) match {
            case Some(mediaTypes) =>
              current.updated(mediaType.mainType, byContentValue.updated(mediaType.contentValue, (mediaType :: mediaTypes)))
            case None =>
              current.updated(mediaType.mainType, byContentValue.updated(mediaType.contentValue, List(mediaType)))
          }
        case None =>
          current.updated(mediaType.mainType, Map(mediaType.contentValue -> List(mediaType)))
      }
      if (!byMainTypeAndContent.compareAndSet(current, updated)) registerByMainTypeAndContent()
    }
    registerByMainAndSubTypeType()
    registerByMainTypeAndContent()
  }

  def find(mediaTypeValue: String): Option[AlmMediaType] = {
    mediaTypeValue.split("/") match {
      case Array(main, sub) =>
        val current = byMainAndSubTypes.get()
        current.get(main).flatMap(_.get(sub))
      case _ => None
    }
  }
  
  override def iterator: Iterator[AlmMediaType] = {
    val current = byMainAndSubTypes.get()
    current.values.flatMap(_.values).toIterator
  }

  def nonIanaItarator: Iterator[AlmMediaType] = iterator.filterNot(_.ianaRegistered)
}