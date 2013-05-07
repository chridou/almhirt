package almhirt.http

import scalaz.syntax.validation._
import almhirt.common.AlmValidation
import almhirt.common.UnspecifiedProblem

trait HttpMarshaller[T] {
  def apply(from: T, toChannel: String): AlmValidation[HttpContent] = marshal(from, toChannel)
  def marshal(from: T, toChannel: String): AlmValidation[HttpContent]
}

object HttpMarshaller {
  def dummy[T](returns : => AlmValidation[HttpContent]) = new HttpMarshaller[T]{
    override def marshal(from: T, toChannel: String) = returns
  }
  
  def alwaysFails[T] = new HttpMarshaller[T]{
    override def marshal(from: T, toChannel: String) = UnspecifiedProblem("I always fail!").failure
  }
}