package riftwarp

import language.higherKinds
import scala.collection.generic.CanBuildFrom

trait CanBuildFroms {
  implicit def canBuildFromTraversable2List[T]: CanBuildFrom[Traversable[_], T, List[T]] =
    new CanBuildFrom[Traversable[_], T, List[T]] {
      def apply() = List.newBuilder[T]
      def apply(from: Traversable[_]) = List.newBuilder[T]
    }
  implicit def canBuildFromTraversable2Vector[T]: CanBuildFrom[Traversable[_], T, Vector[T]] =
    new CanBuildFrom[Traversable[_], T, Vector[T]] {
      def apply() = Vector.newBuilder[T]
      def apply(from: Traversable[_]) = Vector.newBuilder[T]
    }
  implicit def canBuildFromTraversable2Set[T]: CanBuildFrom[Traversable[_], T, Set[T]] =
    new CanBuildFrom[Traversable[_], T, Set[T]] {
      def apply() = Set.newBuilder[T]
      def apply(from: Traversable[_]) = Set.newBuilder[T]
    }

  //  implicit def travOnce2List[T](): CanBuildFrom[TraversableOnce[T], T, List[T]] = 
  //    new CanBuildFrom[TraversableOnce[T], T, List[T]]{
  //      def apply() = List.newBuilder[T]
  //      def apply(from: TraversableOnce[T])= List.newBuilder[T]
  //  }
  //  
  //  implicit def travOnce2Vector[T](): CanBuildFrom[TraversableOnce[T], T, Vector[T]] = 
  //    new CanBuildFrom[TraversableOnce[T], T, Vector[T]]{
  //      def apply() = Vector.newBuilder[T]
  //      def apply(from: TraversableOnce[T])= Vector.newBuilder[T]
  //  }
}