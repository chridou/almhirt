package riftwarp.automatic

import scala.reflect.macros.Context
import almhirt.common._
import riftwarp._

object GeneratedDecomposerImpl {
  

  
  final def decomposeUncurried[TTWhat <: AnyRef: c.WeakTypeTag, TTDimension <: RiftDimension](c: Context)(what: c.Expr[TTWhat], into: c.Expr[Dematerializer[TTDimension]]): c.Expr[AlmValidation[Dematerializer[TTDimension]]] = {
    import c.universe._
    import scala.collection.mutable.Queue
    
    val tWhat = c.weakTypeOf[TTWhat]
    
    //val im = what.m
    
    val getterSymbols = 
      Queue(tWhat.members.filter(m => m.isTerm && !m.isMethod && m.isPublic).map(sym => sym.asTerm.getter).filterNot(_ == NoSymbol))


   // private def getPri
    ???
  }
}