package almhirt.corex.slick

import akka.actor.Props
import almhirt.common._

trait SlickCreationParams {
  def props: Props
  def initAction: () => AlmValidation[Unit]
  def closeAction: () => AlmValidation[Unit]
}