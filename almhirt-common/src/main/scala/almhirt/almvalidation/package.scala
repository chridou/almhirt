package almhirt

import scalaz.Validation

package object validation {
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

  
  object syntax extends ToProblemOps with ToAlmValidationOps
  object problemsyntax extends ToProblemOps
  object almvalidationsyntax extends ToAlmValidationOps
  
  object instances extends ProblemInstances with ProblemCategoryInstances with SeverityInstances with AlmValidationInstances
  object probleminstances extends ProblemInstances with ProblemCategoryInstances with SeverityInstances
  object almvalidationinstances extends AlmValidationInstances

  object functions extends ProblemFunctions with AlmValidationFunctions with AlmValidationParseFunctions
  object problemfunctions extends ProblemFunctions
  object almvalidationfunctions extends AlmValidationFunctions with AlmValidationParseFunctions

  object imports 
    extends ProblemFunctions with AlmValidationFunctions with AlmValidationParseFunctions
    with  ProblemInstances with ProblemCategoryInstances  with SeverityInstances with AlmValidationInstances
    with ToProblemOps with ToAlmValidationOps
    
  object problemimports extends ProblemFunctions with  ProblemInstances with ProblemCategoryInstances  with SeverityInstances with AlmValidationInstances  with ToProblemOps
  object almvalidationimports extends AlmValidationFunctions with AlmValidationParseFunctions with AlmValidationInstances with ToAlmValidationOps 
    
}