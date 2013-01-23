/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.messaging

import java.util.UUID
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import akka.util.Timeout._
import akka.pattern._
import akka.util._
import akka.dispatch._
import almhirt.core._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.messaging.impl.MessageChannelActor
import almhirt.environment.AlmhirtSystem
import almhirt.environment.configuration.ConfigPaths
import almhirt.environment.configuration.ConfigHelper
import almhirt.almakka.ActorBased
import almhirt.common.AlmFuture

trait MessageChannel[T <: AnyRef] extends MessageStream[T] with CanDeliverMessages[T] with CanCreateSubChannels[T] with ActorBased

object MessageChannel {
  def apply[T <: AnyRef](actor: ActorRef, hasExecutionContext: HasExecutionContext)(implicit m: ClassTag[T]): MessageChannel[T] = {
    new ActorBasedMessageChannelImpl[T](actor)(hasExecutionContext, m)
  }

  def apply[T <: AnyRef](name: String)(implicit almhirtsystem: AlmhirtSystem, m: ClassTag[T]): MessageChannel[T] = {
    val actor =
      ConfigHelper.tryGetDispatcherNameFromRootConfig(almhirtsystem.config)(ConfigPaths.messagechannels) match {
        case None => almhirtsystem.actorSystem.actorOf(Props[MessageChannelActor], name = name)
        case Some(dn) => almhirtsystem.actorSystem.actorOf(Props[MessageChannelActor].withDispatcher(dn), name = name)
      }
    actor ! UseAlmhirtSystemMessage(almhirtsystem)
    apply[T](actor, almhirtsystem)(m)
  }

  class ActorBasedMessageChannelImpl[T <: AnyRef](val actor: ActorRef)(implicit hasExecutionContext: HasExecutionContext, m: ClassTag[T]) extends MessageChannel[T] {
    def <-*(handler: Message[T] => Unit, classifier: Message[T] => Boolean)(implicit atMost: FiniteDuration): AlmFuture[RegistrationHolder] = {
      val filter = MessagePredicate[T](classifier)
      def wrappedHandler(message: Message[AnyRef]): Unit =
        handler(message.asInstanceOf[Message[T]])
      (actor ? SubscribeQry(new MessagingSubscription { val predicate = filter; val handler = wrappedHandler _ }))(atMost).mapTo[SubscriptionRsp].map(_.registration)(hasExecutionContext)
    }

    def createSubChannel[TPayload <: T](name: String, classifier: Message[TPayload] => Boolean)(implicit atMost: FiniteDuration, m: ClassTag[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      val filter = MessagePredicate[TPayload](classifier)
      (actor ? CreateSubChannelQry(name, filter))(atMost)
        .mapTo[NewSubChannelRsp]
        .map(subchannel => subchannel.channel)(hasExecutionContext).mapToAlmFuture[ActorRef]
        .map(newActor => MessageChannel[TPayload](newActor, hasExecutionContext))
    }

    def post[U <: T](message: Message[U]) = actor ! PostMessageCmd(message)

    def close() {}

  }

}