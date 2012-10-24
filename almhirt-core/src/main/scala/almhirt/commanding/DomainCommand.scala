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

trait DomainCommand {
  def ticket: Option[String]
  /** The affected aggregate root 
   */
  def aggRootRef: Option[almhirt.domain.AggregateRootRef] 
  
  def isMutator = aggRootRef.isDefined
  def isCreator = aggRootRef.isEmpty
}

trait MutatorCommandStyle{ self: DomainCommand => 
  def id: java.util.UUID
  def version: Long
  def aggRootRef = Some(almhirt.domain.AggregateRootRef(id, version))
}

trait CreatorCommandStyle{ self: DomainCommand => 
  def aggRootRef = None
}
