package almhirt.environment

import akka.pattern._
import almhirt._
import almhirt.almfuture.all._
import almhirt.util._
import almhirt.syntax.almvalidation._
import almhirt.commanding._
import almhirt.messaging._
import test._

object ChannelWiringProblems extends AlmhirtEnvironmentTestKit {
  implicit val atMost = akka.util.Duration(1, "s")//> atMost  : akka.util.FiniteDuration = 1 second
  inTestEnvironment { implicit env =>
    implicit val execContext = env.context.system.futureDispatcher
    var hit1 = false
    var hit2 = false
    val subscription1 = (env.context.problemChannel <-* { x => hit1 = true }).awaitResult(atMost).forceResult
    val subscription2 =
      (env.context.problemChannel.actor ? SubscribeQry(MessagingSubscription.typeBasedHandler[Problem] { x => hit2 = true }))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
        .awaitResult
        .forceResult
    env.reportProblem(UnspecifiedProblem("prob"))
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