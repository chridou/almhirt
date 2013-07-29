package almhirt.testkit.eventlog

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import akka.testkit.TestProbe
import almhirt.testkit._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.HasAlmhirt
import almhirt.common._

abstract class EventLogSpecTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with FunSpec
  with ShouldMatchers { self: CreatesDomainEventLog =>
    
  }