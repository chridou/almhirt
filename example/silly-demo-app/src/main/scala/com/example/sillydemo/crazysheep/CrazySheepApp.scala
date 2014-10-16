package com.example.sillydemo.crazysheep

import akka.actor._
import almhirt.context.AlmhirtContext

class CrazySheepApp(implicit override val almhirtContext: AlmhirtContext) extends CrazySheepActor {

  def receive: Receive = {
    case "Start" =>
      (1 to 5).foreach(n => context.actorOf(Props(new CrazyLilly), s"crazy-lilly-$n"))
      (1 to 5).foreach(n => context.actorOf(Props(new CrazyShawn), s"crazy-shawn-$n"))
      (1 to 5).foreach(n => context.actorOf(Props(new CrazyFred), s"crazy-fred-$n"))
  }
  
  override def preStart() {
    self ! "Start"
  }
}