package almhirt

import almhirt.common._

package object domain {
  /** Used by the [[AggregateRootNexus]] to choose which [[AggregateRootHive]] is to be used by
   *  the [[AggregateRootNexus]] for a given [[almhirt.common.AggregateRootCommand]]
   *  
   *  The selector is a sequence of pairs where the first element of a
   *  pair is the hive(expressed through a [[HiveDescriptor]] that is 
   *  selected via the predicate in the second element of the pair.
   *  
   *  All [[HiveDescriptor]]s must be unique. Furthermore the mapping from a predicate to a 
   *  [[HiveDescriptor]] must be isomorphic.
   *  
   *  Be careful when using hashcodes for selecting hives. They can be negative.
   */
  type HiveSelector = Seq[(HiveDescriptor, AggregateRootCommand ⇒ Boolean)]
  
  implicit class AggregateRootEventOps(self: AggregateRootEvent) {
    def specific[E <: AggregateRootEvent](implicit tag: scala.reflect.ClassTag[E]): AlmValidation[E] = {
      almhirt.almvalidation.funs.almCast[E](self)
    }
      
    def specificUnsafe[E <: AggregateRootEvent](implicit tag: scala.reflect.ClassTag[E]): E = {
      almhirt.almvalidation.funs.almCast[E](self).fold(
        fail ⇒ throw WrongAggregateRootEventTypeException(self, tag),
        succ ⇒ succ)
    }
    def specificUnsafeWithHandler[E <: AggregateRootEvent](f: AggregateRootDomainException ⇒ Nothing)(implicit tag: scala.reflect.ClassTag[E]): E = {
      almhirt.almvalidation.funs.almCast[E](self).fold(
        fail ⇒ f(WrongAggregateRootEventTypeException(self, tag)),
        succ ⇒ succ)
    }
  }
}