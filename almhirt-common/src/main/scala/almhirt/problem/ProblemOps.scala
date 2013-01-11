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
package almhirt.problem

import scala.language.implicitConversions

import scalaz.Scalaz.ToFoldableOps
import scalaz.syntax.Ops
import scalaz.NonEmptyList
import almhirt.common._

trait ProblemOps0 extends Ops[NonEmptyList[Problem]] {
  import inst._

  def aggregate(msg: String): AggregateProblem = {
    val severity = self.map(_.severity).concatenate
    if (self.list.exists(p => p.isSystemProblem))
      AggregateProblem(msg, severity = severity, category = SystemProblem, problems = self.list)
    else
      AggregateProblem(msg, severity = severity, category = ApplicationProblem, problems = self.list)
  }

  def aggregate(): AggregateProblem = aggregate("One or more problems. See causes.")
}

trait ProblemOps1[T <: Problem] extends Ops[T] {

  def toAggregate: AggregateProblem =
    AggregateProblem(self.message, severity = self.severity, category = self.category, problems = self :: Nil)
}

trait ProblemOps2[T <: Problem] extends Ops[T] {
  def withIdentifier(ident: String): T =
    if (ident.trim().isEmpty())
      self
    else
      self.withArg("ident", ident).asInstanceOf[T]

  def markLogged(): T = self.withArg("isLogged", true).asInstanceOf[T]
  def isLogged(): Boolean = self.args.contains("isLogged") && self.args("isLogged") == true

  def setTag(tag: String): T = self.withArg("tag", tag).asInstanceOf[T]
  def isTagged(): Boolean = self.args.contains("tag") && self.args("tag").isInstanceOf[String]
  def tryGetTag(): Option[String] = if (isTagged) Some(self.args("tag").asInstanceOf[String]) else None
}

trait ToProblemOps {
  implicit def ToProblemOps0(a: NonEmptyList[Problem]) = new ProblemOps0 { def self = a }
  implicit def ToProblemOps1[T <: Problem](a: T) = new ProblemOps1[T] { def self = a }
  implicit def ToProblemOps2[T <: Problem](a: T) = new ProblemOps2[T] { def self = a }
}

