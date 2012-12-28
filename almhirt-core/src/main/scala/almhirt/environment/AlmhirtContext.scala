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
package almhirt.environment

import almhirt.core._
import almhirt.common._
import almhirt.commanding._
import almhirt.parts._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.util._
import com.typesafe.config.Config
import almhirt.commanding.CommandEnvelope
import almhirt.parts.HasRepositories
import org.joda.time.DateTime
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.core.impl.SimpleConcurrentServiceRegistry
import akka.dispatch.MessageDispatcher

trait AlmhirtContext extends AlmhirtBaseOps with Disposable {
  def messageHub: MessageHub
  def commandChannel: MessageChannel[CommandEnvelope]
  def domainEventsChannel: MessageChannel[DomainEvent]
  def problemChannel: MessageChannel[Problem]
  def operationStateChannel: MessageChannel[OperationState]
}

object AlmhirtContext {
  import akka.pattern._
  import almhirt.syntax.almvalidation._
  import almhirt.almfuture.all._
  def apply()(implicit sys: AlmhirtSystem): AlmFuture[AlmhirtContext] = {
    implicit val atMost = sys.mediumDuration
    implicit val executionContext = sys.executionContext

    val hub = MessageHub("messageHub")

    for {
      cmdChannel <- hub.createMessageChannel[CommandEnvelope]("commandChannel")
      domEventsChannel <- hub.createMessageChannel[DomainEvent]("domainEventsChannel")
      opStateChannel <- hub.createMessageChannel[OperationState]("operationStateChannel")
      probChannel <- hub.createMessageChannel[Problem]("problemChannel")
    } yield (
      new AlmhirtContext {
        val messageHub = hub
        val commandChannel = cmdChannel
        val problemChannel = probChannel
        val domainEventsChannel = domEventsChannel
        val operationStateChannel = opStateChannel

        def reportProblem(prob: Problem) { broadcast(prob) }
        def reportOperationState(opState: OperationState) { broadcast(opState) }
        def broadcastDomainEvent(event: DomainEvent) { broadcast(event) }
        def postCommand(comEnvelope: CommandEnvelope) { broadcast(comEnvelope) }
        def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { messageHub.broadcast(createMessage(payload, metaData)) }
        def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Message[T] = {
          val header = MessageHeader(sys.getUuid, None, metaData, sys.getDateTime)
          Message(header, payload)
        }

        val executionContext = sys.executionContext
        def shortDuration = sys.shortDuration
        def mediumDuration = sys.mediumDuration
        def longDuration = sys.longDuration

        def getDateTime = sys.getDateTime
        def getUuid = sys.getUuid

        def dispose = {
          messageHub.close
          cmdChannel.close
          opStateChannel.close
          probChannel.close
          domEventsChannel.close
        }
      })
  }
}
