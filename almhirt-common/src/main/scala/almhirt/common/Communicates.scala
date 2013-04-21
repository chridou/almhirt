package almhirt.common

trait CommunicatesOneWay[-A] {
  def send(what: A): Unit
}

trait Communicates[-A, +B] extends CommunicatesOneWay[A]{
  def ask(what: A): AlmFuture[B]
}