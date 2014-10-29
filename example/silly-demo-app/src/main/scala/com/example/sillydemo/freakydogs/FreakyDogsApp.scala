package com.example.sillydemo.freakydogs

import akka.actor._
import almhirt.context.AlmhirtContext

class FreakyDogsApp(implicit override val almhirtContext: AlmhirtContext) extends FreakyDogsActor {

  def receive: Receive = {
    case "Start" ⇒
      (1 to 5).foreach(n ⇒ context.actorOf(Props(new FreakyJill), s"freaky-jill-$n"))
      (1 to 5).foreach(n ⇒ context.actorOf(Props(new FreakySpike), s"freaky-spike-$n"))
  }
  
  override def preStart() {
    self ! "Start"
  }
}