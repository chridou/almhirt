package almhirt.common

trait HasCommunicationTimeout {
  def communicationTimeout: scala.concurrent.duration.Duration
}

trait CommunicatesOneWay[-A] {
  def send(what: A): Unit
}

trait CommunicatesTwoWay[-A, +B] {
  def ask(what: A): AlmFuture[B]
  def askForReply(what: A, replyTo: AlmValidation[B] => Unit): Unit
}

trait Communicates[-A, +B] extends CommunicatesOneWay[A] with CommunicatesTwoWay[A, B]

//trait DefaultTwoWayCommunicator[-A, +B] extends CommunicatesTwoWay[A, B] {
//  
//  override def askForReply(what: A, replyTo: AlmValidation[B] => Unit) {
//    ask(what).onComplete(replyTo)
//  }
//}