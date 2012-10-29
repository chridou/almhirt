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

import almhirt._
import almhirt.commanding._
import almhirt.parts._
import almhirt.domain.DomainEvent
import almhirt.messaging.MessageChannel
import almhirt.messaging.MessageHub
import almhirt.OperationState
import almhirt.Problem
import com.typesafe.config.Config
import almhirt.commanding.CommandEnvelope
import almhirt.parts.HasRepositories

trait AlmhirtContextOps {
  def reportProblem(prob: Problem): Unit
  def reportOperationState(opState: OperationState): Unit
}

trait AlmhirtContext extends AlmhirtContextOps with Disposable {
  def config: Config
  def system: AlmhirtSystem
  def messageHub: MessageHub
  def commandChannel: MessageChannel[CommandEnvelope]
  def domainEventsChannel: MessageChannel[DomainEvent]
  def problemChannel: MessageChannel[Problem]
  def operationStateChannel: MessageChannel[OperationState]

  def problemTopic: Option[String]
}
