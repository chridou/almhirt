package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

trait HttpUnmarshaller[T] {
  def apply(from: HttpContent): AlmValidation[T] = unmarshal(from)
  def unmarshal(from: HttpContent): AlmValidation[T]
}

object HttpUnmarshaller {
  def dummy[T](returns : => AlmValidation[T]) = new HttpUnmarshaller[T]{
    override def unmarshal(from: HttpContent): AlmValidation[T] = returns
  }
  
  def alwaysFails[T] = new HttpUnmarshaller[T]{
    override def unmarshal(from: HttpContent): AlmValidation[T] = UnspecifiedProblem("I always fail!").failure
  }
}