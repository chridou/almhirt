package almhirt.components.impl

//trait InMemoryExecutionStateTracker {
//  protected case class InternalState(tracked: Map[String, TrackingEntry], subscriptionsForFinished: Map[String, Map[ActorRef, LocalDateTime]]) {
//    def addSubscriber(trackId: String, maxWaitTime: FiniteDuration, subscriber: ActorRef): InternalState = {
//      val subscription = (subscriber, theAlmhirt.getUtcTimestamp.plusMillis(maxWaitTime.toMillis.toInt))
//      subscriptionsForFinished.get(trackId) match {
//        case None =>
//          InternalState(this.tracked, this.subscriptionsForFinished + (trackId -> Map(subscription)))
//        case Some(subscriptionForTrackId) =>
//          InternalState(this.tracked, this.subscriptionsForFinished + (trackId -> (subscriptionForTrackId + (subscription))))
//      }
//    }
//
//    def getSubscriptions(trackId: String): Iterable[ActorRef] =
//      subscriptionsForFinished.get(trackId).map(_.keys).getOrElse(Iterable.empty)
//
//    def removeAllFor(trackId: String): InternalState =
//      InternalState(this.tracked - trackId, this.subscriptionsForFinished - trackId)
//
//    def removeSubscribersFor(trackId: String): InternalState =
//      InternalState(this.tracked, this.subscriptionsForFinished - trackId)
//
//    def potentiallyChangeState(executionState: ExecutionState): InternalState =
//      tracked.get(executionState.trackId) match {
//        case None =>
//          InternalState(this.tracked + (executionState.trackId -> TrackingEntry(theAlmhirt.getUtcTimestamp, executionState)), this.subscriptionsForFinished)
//        case Some(entry) =>
//          if (ExecutionState.compareExecutionState(executionState, entry.currentState) > 0) {
//            val newEntry = TrackingEntry(theAlmhirt.getUtcTimestamp, executionState)
//            InternalState(this.tracked + (executionState.trackId -> newEntry), this.subscriptionsForFinished)
//          } else
//            this
//      }
//
//    def getAllFinishedStatesWithSubscribers: Iterable[(ExecutionFinishedState, Iterable[ActorRef])] = {
//      val finishedStates = tracked.map(_._2).map(_.tryGetFinished).flatten
//      finishedStates.map(fState =>
//        (fState, subscriptionsForFinished.get(fState.trackId).map(_.keys).getOrElse(Vector.empty)))
//    }
//  }
//
//  private def transitionToNextState(currentState: InternalState): Receive = {
//    case st: ExecutionState =>
//      val nextState = currentState.potentiallyChangeState(st)
//      postProcess(nextState)
//    case GetExecutionStateFor(trackId) =>
//      currentState.tracked.get(trackId) match {
//        case Some(state) => sender ! CurrentExecutionState(trackId, state.currentState)
//        case None => sender ! ExecutionStateNotTracked(trackId)
//      }
//      postProcess(currentState)
//    case RegisterForFinishedState(trackId, toRegister, maxWaitTime) =>
//      val nextState = currentState.addSubscriber(trackId, maxWaitTime, toRegister)
//      postProcess(nextState)
//
//  }
//
//  private def postProcess(currentState: InternalState) {
//    val finishedWithSubscribers = currentState.getAllFinishedStatesWithSubscribers
//    finishedWithSubscribers.foreach {
//      case (state, subscribers) =>
//        subscribers.foreach(_ ! FinishedExecutionStateResult(state.trackId, state))
//    }
//    val nextState = finishedWithSubscribers.foldLeft(currentState)((acc, cur) =>
//      acc.removeSubscribersFor(cur._1.trackId))
//    context.become(transitionToNextState(nextState))
//  }
//
//  def initialState = transitionToNextState(InternalState(Map.empty, Map.empty))
//
//
//}