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
import scalaz.Validation

/** Classes and traits needed at other places*/
package object almhirt {
  /** A registration using a UUID as a token */
  type RegistrationUUID = Registration[java.util.UUID]
  
//  type AlmValidation[+α] = ({type λ[α] = Validation[Problem, α]})#λ[α]
//  type AlmValidationSBD[+α] = ({type λ[α] = Validation[SingleBadDataProblem, α]})#λ[α]
//  type AlmValidationMBD[+α] = ({type λ[α] = Validation[MultipleBadDataProblem, α]})#λ[α]
  type AlmValidation[+α] = Validation[Problem, α]
  type AlmValidationSBD[+α] = Validation[SingleBadDataProblem, α]
  type AlmValidationMBD[+α] = Validation[MultipleBadDataProblem, α]
  type AlmValidationSM[+α] = Validation[SingleMappingProblem, α]
  type AlmValidationMM[+α] = Validation[MultipleMappingProblem, α]
  type AlmValidationBRV[+α] = Validation[BusinessRuleViolatedProblem, α]
  type AlmValidationMBRV[+α] = Validation[ManyBusinessRulesViolatedProblem, α]
  type AlmValidationAP[+α] = Validation[AggregateProblem, α]    
}