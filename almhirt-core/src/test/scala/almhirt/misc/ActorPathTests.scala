package almhirt.misc

import akka.actor._
import org.scalatest._

class ActorPathTests extends FunSuite with Matchers {

  test("Build a path from an Address the rest...") {
    val address = Address("akka", "mySystem")
    val pathStr = s"$address/user/myTarget"
    info(pathStr)
    ActorPath.fromString(pathStr)
  }
}

