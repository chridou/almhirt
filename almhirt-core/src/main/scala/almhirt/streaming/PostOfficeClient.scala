package almhirt.streaming

import scala.concurrent.duration._
import akka.actor._
import almhirt.tracking.TrackingTicket

trait PostOfficeClientSettings {
  def abort(forAttempt: Int): Boolean
  def delay(forAttempt: Int): FiniteDuration
  def warn(forAttempt: Int): Boolean
}

object PostOfficeClientSettings {
  def apply(maxRetries: Int, theDelay: FiniteDuration, warnEveryN: Int): PostOfficeClientSettings =
    new PostOfficeClientSettings {
	  def abort(forAttempt: Int): Boolean =
	    forAttempt >= maxRetries
	    
	  def delay(forAttempt: Int): FiniteDuration =
	    theDelay
	    
	  def warn(forAttempt: Int): Boolean =
	    (forAttempt > 0 && forAttempt % warnEveryN == 0)
  }
}

object SequentialPostOfficeDropper {
  def props[TElement](postOffice: PostOffice[TElement], packages: Seq[Seq[TElement]], settings: PostOfficeClientSettings): Props =
    Props(new SequentialPostOfficeClient with Actor with ActorLogging {
      def dropping(toProcess: Seq[Seq[TElement]]): Receive = {
        case Start ⇒ 
          toProcess match {
            case Seq() ⇒
              context.stop(self)
            case next +: rest ⇒
              context.become(dropping(rest))
              sendToPostOfficeUntracked(postOffice, next)(settings)
          }
          
        case m: DeliveryJobDone ⇒ 
           toProcess match {
            case Seq() ⇒
              context.stop(self)
            case next +: rest ⇒
              context.become(dropping(rest))
              sendToPostOfficeUntracked(postOffice, next)(settings)
          }
 
        case m: DeliveryJobNotAccepted ⇒
          log.error("Finally failed with a delivery job.")
          context.stop(self)
           
      }
 
      def receive: Receive = dropping(packages)
      
      override def preStart() {
        self ! Start
      }
    })
    
  private case object Start
}

trait SequentialPostOfficeClient { me: Actor with ActorLogging ⇒

  def sendToPostOfficeUntracked[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement])(implicit settings: PostOfficeClientSettings) {
    sendToPostOffice(postOffice, elements, None)
  }

  def sendToPostOfficeTracked[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement], ticket: TrackingTicket)(implicit settings: PostOfficeClientSettings) {
    sendToPostOffice(postOffice, elements, Some(ticket))
  }

  def sendToPostOffice[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement], ticket: Option[TrackingTicket])(implicit settings: PostOfficeClientSettings) {
    val send = () ⇒ postOffice.deliver(elements, self, ticket)
    send()
    context.become(sending(1, settings, send), false)
  }
   def sendToPostOfficeUntrackedWithAppendix[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement], appendix: Receive)(implicit settings: PostOfficeClientSettings) {
    sendToPostOffice(postOffice, elements, None)
  }
 
  def sendToPostOfficeTrackedWithAppendix[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement], ticket: TrackingTicket, appendix: Receive)(implicit settings: PostOfficeClientSettings) {
    sendToPostOfficeWithAppendix(postOffice, elements, Some(ticket), appendix)
  }

  def sendToPostOfficeWithAppendix[TElement](postOffice: PostOffice[TElement], elements: Seq[TElement], ticket: Option[TrackingTicket], appendix: Receive)(implicit settings: PostOfficeClientSettings) {
    val send = () ⇒ postOffice.deliver(elements, self, ticket)
    send()
    context.become(sending(1, settings, send) orElse appendix, false)
  }
  
  private def sending(attempt: Int, settings: PostOfficeClientSettings, resend: () ⇒ Unit): Receive = {
    case m: DeliveryJobDone ⇒
      context.unbecome()
      self ! m

    case m: DeliveryJobNotAccepted ⇒
      if (settings.warn(attempt)) {
        log.warning(s"A package has not been delivered after $attempt attempts.")
      }

      if (settings.abort(attempt)) {
        context.unbecome()
        self ! m
      } else {
        val delay = settings.delay(attempt)
        context.system.scheduler.scheduleOnce(delay)(resend())(context.system.dispatchers.defaultGlobalDispatcher)
        context.become(sending(attempt + 1, settings, resend))
      }
  }
}

