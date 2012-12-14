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
import riftwarp.RiftWarp

//trait AlmhirtContextOps {
//  def reportProblem(prob: Problem): Unit
//  def reportOperationState(opState: OperationState): Unit
//  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String]): Unit
//  def getDateTime: DateTime
//  def getUuid: java.util.UUID
//  def messageWithPayload[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Message[T]
//}

trait AlmhirtContext extends Disposable {
  def system: AlmhirtSystem
  def messageHub: MessageHub
  def commandChannel: MessageChannel[CommandEnvelope]
  def problemChannel: MessageChannel[Problem]
  def operationStateChannel: MessageChannel[OperationState]
}

object AlmhirtContext {
  import akka.pattern._
  import akka.util.Duration._
  import almhirt.syntax.almvalidation._
  import almhirt.almfuture.all._
  def apply()(implicit sys: AlmhirtSystem): AlmFuture[AlmhirtContext] = {
    implicit val atMost = sys.mediumDuration
    implicit val executionContext = sys.futureDispatcher

//    val serviceRegistry = new SimpleConcurrentServiceRegistry
    
    val hub = MessageHub("messageHub")
    
//    val theRiftWarp = riftwarp.RiftWarp.unsafeWithDefaults
//    almhirt.core.serialization.RiftWarpUtilityFuns.addRiftWarpRegistrations(theRiftWarp)
//    serviceRegistry.registerServiceByType(classOf[RiftWarp], theRiftWarp)
    
    for {
      cmdChannel <- hub.createMessageChannel[CommandEnvelope]("commandChannel")
      opStateChannel <- hub.createMessageChannel[OperationState]("operationStateChannel")
      probChannel <- hub.createMessageChannel[Problem]("problemChannel")
    } yield (
      new AlmhirtContext {
        val config = sys.config
        val system = sys
        val messageHub = hub
        val commandChannel = cmdChannel
        val problemChannel = probChannel
        val operationStateChannel = opStateChannel
        
//        def getServiceByType(clazz: Class[_ <: AnyRef]): AlmValidation[AnyRef]
        
        def dispose = {
          messageHub.close
          cmdChannel.close
          opStateChannel.close
          probChannel.close
          system.dispose
        }
      })
  }
}
