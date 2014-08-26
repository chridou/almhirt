package almhirt

import almhirt.common._

package object domain {
  /** Used by the [[AggregateRootNexus]] to choose which [[AggregateRootHive]] is to be used by
   *  the [[AggregateRootNexus]] for a given [[almhirt.common.AggregateCommand]]
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
  type HiveSelector = Seq[(HiveDescriptor, AggregateCommand ⇒ Boolean)]
  
  implicit class AggregateEventOps(self: AggregateRootEvent) {
    def specific[E <: AggregateRootEvent](implicit tag: scala.reflect.ClassTag[E]): E = {
      almhirt.almvalidation.funs.almCast[E](self).fold(
        fail ⇒ throw WrongAggregateEventTypeException(self, tag),
        succ ⇒ succ)
    }
    def specificWithHandler[E <: AggregateRootEvent](f: AggregateRootDomainException ⇒ Nothing)(implicit tag: scala.reflect.ClassTag[E]): E = {
      almhirt.almvalidation.funs.almCast[E](self).fold(
        fail ⇒ f(WrongAggregateEventTypeException(self, tag)),
        succ ⇒ succ)
    }
  }
}