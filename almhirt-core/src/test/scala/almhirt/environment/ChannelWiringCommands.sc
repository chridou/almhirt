package almhirt.environment

import akka.pattern._
import almhirt._
import almhirt.almfuture.all._
import almhirt.util._
import almhirt.syntax.almvalidation._
import almhirt.commanding._
import almhirt.messaging._
import test._

object ChannelWiringCommands extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(1, "s")//> atMost  : akka.util.FiniteDuration = 1 second
  inTestAlmhirt { implicit almhirt =>
    val env = almhirt.environment
    implicit val execContext = env.context.system.futureDispatcher
    var hit1 = false
    var hit2 = false
    val subscription1 = (env.context.commandChannel <-* { x => hit1 = true }).awaitResult(atMost).forceResult
    val subscription2 =
      (env.context.commandChannel.actor ? SubscribeQry(MessagingSubscription.typeBasedHandler[CommandEnvelope] { x => hit2 = true }))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
        .awaitResult
        .forceResult
    almhirt.executeTrackedCommand(NewTestPerson(env.getUuid, "Jim-ChannelWiringSpecs"), "test")
    env.operationStateTracker.getResultFor("test").awaitResult
    subscription1.dispose()
    subscription2.dispose()
    println(hit1)
    println(hit2)
    hit1 && hit2


  }                                               //> true
                                                  //| true
                                                  //| res0: Boolean = true
}