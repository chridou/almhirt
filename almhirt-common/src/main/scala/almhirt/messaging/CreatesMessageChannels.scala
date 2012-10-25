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

import almhirt._

trait CreatesMessageChannels {
  def createMessageChannel[TPayLoad <: AnyRef](name: Option[String], topic: Option[String])(implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]]
  def createNamedMessageChannel[TPayLoad <: AnyRef](name: String, topic: Option[String])(implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]] =
    createMessageChannel(Some(name), topic)(m)
  def createUnnamedMessageChannel[TPayLoad <: AnyRef](topic: Option[String])(implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]] =
    createMessageChannel(None, topic)(m)
  def createGlobalMessageChannel[TPayLoad <: AnyRef](name: Option[String])(implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]]
  def createNamedGlobalMessageChannel[TPayLoad <: AnyRef](name: String)(implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]] =
    createGlobalMessageChannel(Some(name))(m)
  def createUnnamedGlobalMessageChannel[TPayLoad <: AnyRef](implicit m: Manifest[TPayLoad]): AlmFuture[MessageChannel[TPayLoad]] =
    createGlobalMessageChannel(None)(m)
}