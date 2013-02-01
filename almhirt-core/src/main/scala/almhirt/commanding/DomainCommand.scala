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
package almhirt.commanding

import java.util.UUID
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

trait DomainCommand { /* def id: UUID */ }

trait BoundDomainCommand extends DomainCommand {
  type TEvent <: DomainEvent
  type TAR <: AggregateRoot[TAR, TEvent]
  type TAction <: CommandAction { type Event = TEvent; type AR = TAR }
  def aggRootRef: Option[AggregateRootRef]
  
  def actions: List[TAction]
}

trait CommandAction {
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR,Event]
  
}

object CommandAction {
  implicit class CommanActionOps(act: CommandAction) {
    def isCreator = act.isInstanceOf[CreatorCommandAction]
    def isMutator = act.isInstanceOf[MutatorCommandAction]
  }
}
trait MutatorCommandAction extends CommandAction

trait CreatorCommandAction extends CommandAction

//trait FreestyleCommand extends DomainCommand
