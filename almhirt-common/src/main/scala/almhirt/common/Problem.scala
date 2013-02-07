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
package almhirt.common

trait Problem{
  type T <: Problem
  def message: String
  def severity: Severity
  def category: ProblemCategory
  def args: Map[String, Any]
  def cause: Option[ProblemCause]
 
  def withSeverity(severity: Severity): T
  def withArg(key: String, value: Any): T
  def withMessage(newMessage: String): T
  def withCause(cause: ProblemCause): T
  def mapMessage(mapOp: String => String): T
  
  def isSystemProblem = category == SystemProblem
  
  protected def baseInfo(): StringBuilder = {
      val builder = new StringBuilder()
      builder.append("%s\n".format(this.getClass.getName))
      builder.append("%s\n".format(message))
      builder.append("Category: %s\n".format(category))
      builder.append("Severity: %s\n".format(severity))
      builder.append("Arguments: %s\n".format(args))
      cause match {
      	case None => 
      	  ()
      	case Some(CauseIsThrowable(HasAThrowable(exn))) => 
          builder.append("Message: %s\n".format(exn.toString))
          builder.append("Stacktrace:\n%s\n".format(exn.getStackTraceString))
      	case Some(CauseIsThrowable(desc @ HasAThrowableDescribed(_,_,_,_))) => 
          builder.append("Description: %s\n".format(desc.toString))
      	case Some(CauseIsProblem(prob)) => 
          builder.append("Problem: %s\n".format(prob.toString))
      }
      builder
  }
  
  override def toString() = baseInfo.result
}

trait SecurityProblem extends Problem

