package riftwarp.automatic

import scala.reflect.macros.Context
import almhirt.common._
import riftwarp._

object GeneratedDecomposerImpl {
  def decomposeUncurried[TTWhat <: AnyRef: c.WeakTypeTag, TTDimension <: RiftDimension: c.WeakTypeTag](c: Context)(what: c.Expr[TTWhat], into: c.Expr[Dematerializer[TTDimension]]): c.Expr[AlmValidation[Dematerializer[TTDimension]]] = {
    import c.universe._
    import scala.collection.mutable.Queue
    
    val tWhat = weakTypeOf[TTWhat]
    
    val theFields = 
      Queue(tWhat.members.filter(m => m.isTerm && !m.isMethod && m.isPublic).foreach(term => println("")))
    ???
  }
}