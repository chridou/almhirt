package riftwarp.automatic

import scala.language.experimental.macros

import scala.reflect.macros.Context
import almhirt.common._
import riftwarp._

object GeneratedDecomposerImpl {
  final def decomposeUncurried[TTWhat <: AnyRef, TTDimension <: RiftDimension](what: TTWhat, into: Dematerializer[TTDimension]): AlmValidation[Dematerializer[TTDimension]] = ??? // macro decomposeUncurriedImpl[TTWhat, TTDimension]

//  final def decomposeUncurriedImpl[TTWhat <: AnyRef: c.WeakTypeTag, TTDimension <: RiftDimension](c: Context)(what: c.Expr[TTWhat], into: c.Expr[Dematerializer[TTDimension]]): c.Expr[AlmValidation[Dematerializer[TTDimension]]] = {
//    import c.universe._
//    import scala.collection.mutable.Queue
//
//    val tWhat = c.weakTypeOf[TTWhat]
//
//    //val im = what.m
//
//   
//    val fields = Queue(
//      tWhat.members
//        .filter(m => m.isTerm && !m.isMethod && m.isPublic)
//        .map(sym => sym.asTerm.getter)
//        .filterNot(getter => getter == NoSymbol /*|| getter.name. == "riftDescriptor"*/).toSeq: _*)
//
//    // private def getPri
//    ???
//  }
//
//  //  final def getPrimitiveAdder[TTWhat <: AnyRef](c: Context)(what: c.Expr[TTWhat]
}