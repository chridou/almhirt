package almhirt.akkax.reporting

import almhirt.akkax.reporting.AST.RValue
import almhirt.akkax.reporting.AST.RValue

/**
 * @author douven
 */
object NumericOps {
  def floatOp[T: RValueConverter](a: AST.RValue, b: AST.RValue)(op: (Double, Double) ⇒ T): AST.RValue = {
    (a, b) match {
      case (AST.RError(_), _)                 ⇒ AST.RError("No operation when the first operator is an error.")
      case (_, AST.RError(_))                 ⇒ AST.RError("No operation when the second operator is an error.")
      case (AST.RNotAvailable, _)             ⇒ AST.RNotAvailable
      case (_, AST.RNotAvailable)             ⇒ AST.RNotAvailable
      case (AST.RFloat(a), AST.RFloat(b))     ⇒ toAST(op(a, b))
      case (AST.RInteger(a), AST.RFloat(b))   ⇒ toAST(op(a.toDouble, b))
      case (AST.RFloat(a), AST.RInteger(b))   ⇒ toAST(op(a, b.toDouble))
      case (AST.RInteger(a), AST.RInteger(b)) ⇒ toAST(op(a.toDouble, b.toDouble))
      case _                                  ⇒ AST.RError("Incompatible types.")
    }
  }

  def intOp[T: RValueConverter](a: AST.RValue, b: AST.RValue)(op: (Long, Long) ⇒ T): AST.RValue = {
    (a, b) match {
      case (AST.RError(_), _)                 ⇒ AST.RError("No operation when the first operator is an error.")
      case (_, AST.RError(_))                 ⇒ AST.RError("No operation when the second operator is an error.")
      case (AST.RNotAvailable, _)             ⇒ AST.RNotAvailable
      case (_, AST.RNotAvailable)             ⇒ AST.RNotAvailable
      case (AST.RFloat(a), AST.RFloat(b))     ⇒ toAST(op(a.toLong, b.toLong))
      case (AST.RInteger(a), AST.RFloat(b))   ⇒ toAST(op(a, b.toLong))
      case (AST.RFloat(a), AST.RInteger(b))   ⇒ toAST(op(a.toLong, b))
      case (AST.RInteger(a), AST.RInteger(b)) ⇒ toAST(op(a, b))
      case _                                  ⇒ AST.RError("Incompatible types.")
    }
  }
}