package com.example.sillydemo

import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.corex.spray.service.HttpHerderService
import spray.routing.HttpService
import spray.routing.Directives
import akka.actor._
import almhirt.context.HasAlmhirtContext


trait SillyHerderService
  extends AlmHttpEndpoint
  with HttpHerderService
  with HttpService
  with Directives { self: Actor with ActorLogging with HasAlmhirtContext =>

  def route = herderTerminator
}