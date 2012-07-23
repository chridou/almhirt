package almhirt.almakka

import scalaz.{Success, Failure}
import akka.event._
import almhirt.validation._

trait AlmActorLogging { self: akka.actor.Actor =>
  val log = Logging(context.system, this)
  
  implicit def almValidation2AlmValidationLoggingW[T](validation: AlmValidation[T]) = new AlmValidationLoggingW[T](validation)
  final class AlmValidationLoggingW[T](validation: AlmValidation[T]) {
    def logFailure(): AlmValidation[T] = {
      validation match {
        case Success(_) => 
          validation
        case Failure(f) =>
          f.severity match {
            case Minor =>
              log.warning(f.toString)
            case Major =>
              log.error(f.toString)
            case Critical =>
              log.error(f.toString)
          }
          validation
      }
    }
  }
}