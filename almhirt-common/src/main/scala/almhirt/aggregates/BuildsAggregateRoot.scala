package almhirt.aggregates

import almhirt.common._

trait BuildsAggregateRoot[T <: AggregateRoot, E <: AggregateEvent] {
  def applyEvent(agg: T, event: E): Postnatalis[T]

  def fromEvent(event: E): T

  final def applyLifecycleAgnostic(agg: AggregateRootState[T], event: E): AggregateRootState[T] = {
    agg match {
      case Alive(a) ⇒ applyEvent(a, event)
      case NeverExisted ⇒ Alive(fromEvent(event))
      case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
    }
  }

  def applyEvents(agg: T, events: Iterable[E]): Postnatalis[T] =
    events.foldLeft(Alive(agg): Postnatalis[T]) {
      case (state, next) ⇒
        state match {
          case Alive(st) ⇒
            applyEvent(st, next)
          case Dead(id, v) ⇒
            throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
        }
    }

  final def applyPostnatalis(livingAgg: Postnatalis[T], event: E): Postnatalis[T] =
    livingAgg match {
      case Alive(a) ⇒ applyEvent(a, event)
      case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
    }

  def rebuildFromHistory(events: Iterable[E]): AggregateRootState[T] =
    if (events.isEmpty) {
      NeverExisted
    } else {
      applyEvents(fromEvent(events.head), events.tail)
    }

}

trait BuildsAggregetaRootAntemortem[T <: AggregateRoot, E <: AggregateEvent] extends BuildsAggregateRoot[T, E] {
  final def applyEvent(agg: T, event: E): Postnatalis[T] =
    applyEventAntemortem(Alive(agg), event)

  final def fromEvent(event: E): T =
    applyLifecycleAgnostic(NeverExisted, event) match {
      case Alive(a) ⇒ a
      case NeverExisted ⇒ throw new Exception(s"An event must create an aggregate root.")
      case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
    }

  def applyEventAntemortem(state: Antemortem[T], event: E): Postnatalis[T]
}

trait BuildsAggregetaRootFromEventHandlers[T <: AggregateRoot, E <: AggregateEvent] extends BuildsAggregateRoot[T, E] {
  override def applyEvent(agg: T, event: E) =
    if (event.aggId != agg.id) {
      throw new Exception(s"Aggregate root ids do not match: ${agg.id} does not equal ${event.aggId}.")
    } else if (event.aggVersion != agg.version) {
      throw new Exception(s"Aggregate root versions do not match: ${agg.version} does not equal ${event.aggVersion}.")
    } else {
      mutateHandler(agg, event)
    }

  override def fromEvent(event: E): T =
    if (event.aggVersion != AggregateRootVersion(0L)) {
      throw new Exception(s"Event version has to be 0 but is ${event.aggVersion}.")
    } else {
      createHandler(event)
    }

  /**
   * The implementation must increase the version!
   */
  def createHandler: PartialFunction[E, T]

  /**
   * The implementation must increase the version!
   */
  def mutateHandler: (T, E) ⇒ Postnatalis[T]
}
