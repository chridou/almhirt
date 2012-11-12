package almhirt.environment

import akka.pattern._
import almhirt._
import almhirt.almfuture.all._
import almhirt.util._
import almhirt.syntax.almvalidation._
import almhirt.commanding._
import almhirt.messaging._
import test._

object ChannelWiringOpState extends AlmhirtEnvironmentTestKit {
  implicit val atMost = akka.util.Duration(1, "s")//> atMost  : akka.util.FiniteDuration = 1 second
  inTestEnvironment { implicit env =>
   implicit val execContext = env.context.system.futureDispatcher
    var hit1 = false
    var hit2 = false
    val subscription1 = (env.context.operationStateChannel <-* { x => hit1 = true }).awaitResult(atMost).forceResult
    val subscription2 =
      (env.context.operationStateChannel.actor ? SubscribeQry(MessagingSubscription.typeBasedHandler[OperationState]{ x => hit2 = true }))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
        .awaitResult
        .forceResult
    env.broadcast(InProcess("opstatetestInProcess"))
    env.operationStateTracker.getResultFor("opstatetestInProcess").awaitResult
    subscription1.dispose()
    subscription2.dispose()
    println(hit1)
    println(hit2)
    hit1 && hit2
    
  }                                               //> BBBBBBBBBBBBBBBBBBBBroadcast: Message(MessageHeader(0e7c1352-a26d-4ae0-afe9
                                                  //| -181d618a8a5e,None,Map(),2012-11-12T09:51:51.196+01:00),InProcess(StringTra
                                                  //| ckingTicket(opstatetestInProcess)))
                                                  //| PPPPPPPPPPPPPPPPPPPPPPPPPPPPPpost: Message(MessageHeader(0e7c1352-a26d-4ae0
                                                  //| -afe9-181d618a8a5e,None,Map(),2012-11-12T09:51:51.196+01:00),InProcess(Stri
                                                  //| ngTrackingTicket(opstatetestInProcess)))
                                                  //| akka://almhirt/user/messageHub/operationStateChannel
                                                  //| DDDDDDDDDDDDDDDDDDISPATCH: Message(MessageHeader(0e7c1352-a26d-4ae0-afe9-18
                                                  //| 1d618a8a5e,None,Map(),2012-11-12T09:51:51.196+01:00),InProcess(StringTracki
                                                  //| ngTicket(opstatetestInProcess)))
                                                  //| akka://almhirt/user/messageHub/operationStateChannel
                                                  //| DDDDDDDDDDDDDDDDDDISPATCH: Message(MessageHeader(0e7c1352-a26d-4ae0-afe9-18
                                                  //| 1d618a8a5e,None,Map(),2012-11-12T09:51:51.196+01:00),InProcess(StringTracki
                                                  //| ngTicket(opstatetestInProcess)))
                                                  //| akka://almhirt/user/messageHub/operationStateChannel
                                                  //| DDDDDDDDDDDDDDDDDDISPATCH: Message(MessageHeader(0e7c1352-a26d-4ae0-afe9-18
                                                  //| 1d618a8a5e,None,Map(),2012-11-12T09:51:51.196+01:00),InProcess(StringTracki
                                                  //| ngTicket(opstatetestInProcess)))
                                                  //| akka://almhirt/user/messageHub/operationStateChannel
                                                  //| true
                                                  //| true
                                                  //| res0: Boolean = true
}