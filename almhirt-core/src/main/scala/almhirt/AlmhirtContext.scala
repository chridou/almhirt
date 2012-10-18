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
package almhirt

import almhirt._
import almhirt.domain.DomainEvent
import almhirt.messaging.{MessageHub, MessageChannel}
import almhirt.almakka.AlmAkkaContext
import almhirt.commanding.DomainCommand

trait AlmhirtContext extends AlmAkkaContext {
  def reportProblem(problem: Problem): Unit
  def findService[T]: AlmValidation[T]
  def messageHub: MessageHub
  def commandChannel: MessageChannel[DomainCommand]
  def domainEventsChannel: MessageChannel[DomainEvent]
  def problemChannel: MessageChannel[Problem]
  def operationStateChannel: MessageChannel[OperationState]
}

//import almhirt.almakka.AlmAkka
//
//object Almhirt {
//  private var eventChannel = 1
//  private var commandChannel = 1
//  private var messagingChannel = 1
//  
//  val actorSystem = AlmAkka.actorSystem
//}