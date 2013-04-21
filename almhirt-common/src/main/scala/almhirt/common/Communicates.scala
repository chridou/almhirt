package almhirt.common

trait HasCommunicationTimeout {
  def communicationTimeout: scala.concurrent.duration.Duration
}

trait CommunicatesOneWay[-A] {
  def send(what: A): Unit
}

trait CommunicatesTwoWay[-B, +C] {
  def ask(what: B): AlmFuture[C]
  def askForReply(what: B, replyTo: AlmValidation[C] => Unit): Unit
}

trait Communicates[-A, -B, +C] extends CommunicatesOneWay[A] with CommunicatesTwoWay[B, C]

//trait DefaultTwoWayCommunicator[-A, +B] extends CommunicatesTwoWay[A, B] {
//  
//  override def askForReply(what: A, replyTo: AlmValidation[B] => Unit) {
//    ask(what).onComplete(replyTo)
//  }
//}