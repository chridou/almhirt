package almhirt.corex.spray.routes

import spray.routing._
import spray.routing.directives._

trait CommandEndpoint extends HttpService  {
  val executeRoute = get | put


}