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

  def aggregate(): AggregatedProblem = {
     AggregatedProblem(problems = self.list.toList)
  }
}

trait ProblemOps1[T <: Problem] extends Ops[T] {

  def toAggregate: AggregatedProblem =
    AggregatedProblem(problems = self :: Nil)
}

trait ToProblemOps {
  implicit def ToProblemOps0(a: NonEmptyList[Problem]) = new ProblemOps0 { def self = a }
  implicit def ToProblemOps1[T <: Problem](a: T) = new ProblemOps1[T] { def self = a }
}

