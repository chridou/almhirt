package almhirt.domain

import akka.actor._
import almhirt.common._

import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer

import almhirt.streaming.PostOffice

trait AggregateRootDroneFactory extends Function1[AggregateCommand, AlmValidation[Props]] {
  final def apply(command: AggregateCommand): AlmValidation[Props] = propsForCommand(command)
  def propsForCommand(command: AggregateCommand): AlmValidation[Props]
}

private[almhirt] object AggregateRootHiveInternals {
  case object Start
}

//trait AggregateRootHive { me: Actor with ActorLogging with ActorConsumer =>
//  import AggregateRootHiveInternals._
//  def aggregateCommandProducer: Producer[AggregateCommand]
//
//  def buffersize: Int
//  def droneFactory: AggregateRootDroneFactory
//
//  def receiveInitialize: Receive = {
//    case Start =>
//      val meAsConsumer = ActorConsumer[AggregateCommand](self)
//      aggregateCommandProducer.produceTo(meAsConsumer)
//      request(buffersize)
//  }
//
//  def xx(buffer: Vector[AggregateCommand]): Receive = {
//    case ActorConsumer.OnNext(aggregateCommand: AggregateCommand) =>
//      context.child(aggregateCommand.aggId.value) match {
//        case Some(drone) =>
//          drone ! aggregateCommand
//        case None =>
//          droneFactory(aggregateCommand) match {
//            case scalaz.Success(props) =>
//              val actor = context.actorOf(props, aggregateCommand.aggId.value)
//              context watch actor
//              actor ! aggregateCommand
//          }
//      }
//    case ActorConsumer.OnNext(something) =>
//      log.warning(s"Received something I cannot handle: $something")
//      request(1)
//  }
//}