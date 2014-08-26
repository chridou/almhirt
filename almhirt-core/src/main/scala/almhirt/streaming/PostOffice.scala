package almhirt.streaming

import akka.actor._
import almhirt.common._
import almhirt.tracking.TrackingTicket

/** Intended to send small sequences of TElement where each Seq[TElement] is treated as an undivisible unit */
trait PostOffice[TElement] {
  def deliver(elements: Seq[TElement], notify: ActorRef, ticket: Option[TrackingTicket])

  final def deliverUntracked(notify: ActorRef, elements: TElement*) {
    deliver(elements, notify, None)
  }

  final def deliverTracked(notify: ActorRef, ticket: TrackingTicket, elements: TElement*) {
    deliver(elements, notify, Some(ticket))
  }
}

object PostOffice {
  def apply[TElement](actor: ActorRef): PostOffice[TElement] = {
    new PostOffice[TElement] {
      def deliver(elements: Seq[TElement], notify: ActorRef, ticket: Option[TrackingTicket]) {
        actor ! PostOfficeInternal.InternalSendPackage(elements, ticket, notify)
      }
    }
  }

  def devNull[TElement]: PostOffice[TElement] =
    new PostOffice[TElement] {
      def deliver(elements: Seq[TElement], notify: ActorRef, ticket: Option[TrackingTicket]) {
        notify ! DeliveryJobDone(ticket)
      }
    }

  def faked[TElement](delegateTo: ActorRef): PostOffice[TElement] =
    new PostOffice[TElement] {
      def deliver(elements: Seq[TElement], notify: ActorRef, ticket: Option[TrackingTicket]) {
        notify ! DeliveryJobDone(ticket)
        elements.foreach(delegateTo ! _)
      }
    }
}

trait PostOfficeStrategyFactory[TElement] {
  /** maxPackageBuffer is the number of Seq[TElements] that can be buffered */
  def create(stockroom: Stockroom[TElement], maxPackageBufferSize: Int): PostOfficeStrategy[TElement]
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
  case object InternalSignContract

  final case class InternalSendPackage(elements: Seq[_], ticket: Option[TrackingTicket], toNotify: ActorRef)
}

/**
 * Base trait for a PostOffice.
 *  Has the required logic to operate via a configurable strategy.
 *
 *  _Users must not change state via context.become but use ActorPostOffice#become_
 */
trait ActorPostOffice[TElement] extends Actor { me: ActorLogging ⇒
  import PostOfficeInternal._

  protected case object PostOfficeClosed

  protected def broker: StreamBroker[TElement]
  protected def createStrategy(stockroom: Stockroom[TElement]): PostOfficeStrategy[TElement]

  /* May only be called from the Actor's dispatcher */
  final protected def sendUntracked(notify: ActorRef, elements: TElement*) {
    send(notify, None, elements)
  }

  /* May only be called from the Actor's dispatcher */
  final protected def sendTracked(notify: ActorRef, ticket: TrackingTicket, elements: TElement*) {
    send(notify, Some(ticket), elements)
  }

  /* May only be called from the Actor's dispatcher */
  final protected def send(notify: ActorRef, ticket: Option[TrackingTicket], elements: Seq[TElement]) {
    if (stockroom != null)
      strategy.stash(elements, ticket, notify)
    else {
      log.warning("Tried to send a package without a stockroom. May the contract has no been signe yet. Rejecting.")
      notify ! DeliveryJobNotAccepted(ticket)
    }

  }

  /** Users must change state only via this method! */
  protected def become(handler: Receive, discardOld: Boolean = true) {
    context.become(handler orElse internalHandlerAppendix, discardOld)
  }

  private var stockroom: Stockroom[TElement] = null
  private var internalHandlerAppendix: Receive = internalUncontractedHandler
  private var strategy: PostOfficeStrategy[TElement] = null

  private def signContract() {
    broker.signContract(new SuppliesContractor[TElement] {
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

        if (log.isDebugEnabled)
          log.debug("My contract expired")
      }
    })
  }

  private def internalContractedHandler: Receive = {
    case InternalDeliverSuppliesNow(amount) ⇒
      strategy.deliverToBroker(amount)
    case InternalOnProblem(problem) ⇒
      sys.error(s"An error occured:\n$problem")
    case InternalContractExpired ⇒
      stockroom = null
      internalHandlerAppendix = internalUncontractedHandler
      self ! PostOfficeClosed
    case InternalSignContract ⇒
      sys.error("I am already contracted.")

  }

  private def internalUncontractedHandler: Receive = {
    case InternalDeliverSuppliesNow(amount) ⇒
      sys.error("I have been ordered to deliver supplies but I'm not contracted to any broker.")
    case InternalOnProblem(problem) ⇒
      sys.error(s"I have been sent a problem but I'm not contracted to any broker. The problem is\n$problem")
    case InternalContractExpired ⇒
      sys.error("I don't have a contract that could have expired.")
    case InternalSignContract ⇒
      sys.error("I cannot be recontracted.")
  }

  private def initPostOffice(): Receive = {
    case InternalSignContract ⇒
      signContract()

    case InternalNewStockroom(stockroom: Stockroom[TElement]) ⇒
      this.stockroom = stockroom
      this.strategy = createStrategy(stockroom)
      internalHandlerAppendix = internalContractedHandler
      become(afterInit)
  }

  final override def receive: Receive = initPostOffice()

  /** This is the first user state that will be entered after initialization */
  def afterInit: Receive

  override def preStart {
    self ! InternalSignContract
    super.preStart
  }
}

/**
 * Add this trait to a PostOffice to make it an actor that does nothing else then taking packages and notifying the sender.
 *
 */
trait PostOfficeLoop[TElement] { me: ActorPostOffice[TElement] with Actor ⇒
  private val mySendLoop: Receive = {
    case PostOfficeInternal.InternalSendPackage(elements: Seq[TElement], ticket, toNotify) ⇒
      this.send(toNotify, ticket, elements)
  }

  final override val afterInit = mySendLoop

}