package almhirt.streaming

import akka.actor._
import almhirt.common._
import almhirt.tracking.TrackingTicket

trait PostOffice[TElement] {
  def deliver(blister: Seq[TElement], notify: ActorRef)
  def deliverTracked(blister: Seq[TElement], ticket: TrackingTicket, notify: ActorRef)
}

trait PostOfficeStrategyFactory[TElement] {
  def create(stockroom: Stockroom[TElement], maxBufferSize: Int): PostOfficeStrategy[TElement]
}

/** Does not have to be thread safe. The PostOffice has to handle the strategy with care. */
trait PostOfficeStrategy[TElement] {
  def stash(elements: Seq[TElement], ticket: Option[TrackingTicket], notify: ActorRef)
  def deliverToBroker(amount: Int)
}

private[almhirt] object PostOfficeInternal {
  import scala.language.existentials
  final case class InternalNewStockroom(stockroom: Stockroom[_])
  final case class InternalDeliverSuppliesNow(amount: Int)
  final case class InternalOnProblem(problem: Problem)
  case object InternalContractExpired
}

/** Base trait for a PostOffice.
 *  Has the required logic to operate via a configurable strategy.
 *  
 *  _Users must not change state via context.become but use ActorPostOffice#become_
 */
trait ActorPostOffice[TElement] { me: Actor with ActorLogging =>
  import PostOfficeInternal._

  protected case object PostOfficeClosed

  protected def broker: SuppliesBroker[TElement]
  protected def createStrategy(stockroom: Stockroom[TElement]): PostOfficeStrategy[TElement]

  private var stockroom: Stockroom[TElement] = null
  private var internalHandlerAppendix: Receive = null
  private var strategy: PostOfficeStrategy[TElement] = null

  broker.signContract(contractor)

  def contractor = new SuppliesContractor[TElement] {
    def onProblem(problem: Problem) = {
      self ! InternalOnProblem(problem)
    }

    def onStockroom(stockroom: Stockroom[TElement]) = {
      self ! InternalNewStockroom(stockroom)
    }

    def onDeliverSuppliesNow(amount: Int) = {
      self ! InternalDeliverSuppliesNow(amount)
    }

    def onContractExpired() = {
      self ! InternalContractExpired
    }
  }

  final protected def send(notify: ActorRef, elements: TElement*) {
    if (stockroom != null)
      strategy.stash(elements, None, notify)
    else
      sys.error("The post office is closed.")
  }

  final protected def sendTracked(notify: ActorRef, ticket: TrackingTicket, elements: TElement*) {
    if (stockroom != null)
      strategy.stash(elements, Some(ticket), notify)
    else
      sys.error("The post office is closed.")
  }

  private def internalContractedHandler: Receive = {
    case InternalDeliverSuppliesNow(amount) =>
      strategy.deliverToBroker(amount)
    case InternalOnProblem(problem) =>
      sys.error(s"An error occured:\n$problem")
    case InternalContractExpired =>
      stockroom = null
      self ! PostOfficeClosed

  }

  private def internalUncontractedHandler: Receive = {
    case InternalDeliverSuppliesNow(amount) =>
      sys.error("I have been ordered to deliver supplies but I'm not contracted to any broker.")
    case InternalOnProblem(problem) =>
      sys.error(s"I have been sent a problem but I'm not contracted to any broker. The problem is\n$problem")
    case InternalContractExpired =>
      sys.error("I don't have a contract that could have expired.")
  }

  /** Users must change state only via this method! */
  protected def become(handler: Receive, discardOld: Boolean = true) {
    context.become(handler orElse internalHandlerAppendix, discardOld)
  }

  private def initPostOffice(): Receive = {
    case InternalNewStockroom(stockroom: Stockroom[TElement]) =>
      this.stockroom = stockroom
      this.strategy = createStrategy(stockroom)
      internalHandlerAppendix = internalContractedHandler
      become(afterInit)
  }

  final override def receive: Receive = initPostOffice()
  
  /** This is the first user state that will be entered after initialization */
  def afterInit: Receive



}