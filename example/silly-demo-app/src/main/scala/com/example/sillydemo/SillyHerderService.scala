package com.example.sillydemo

import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.corex.spray.service.HttpHerderServiceFactory
import spray.routing.HttpService
import spray.routing.Directives
import akka.actor._
import almhirt.context.HasAlmhirtContext
import almhirt.akkax._


trait SillyHerderService
  extends AlmHttpEndpoint
  with HttpHerderServiceFactory
  with HttpService
  with Directives { self: AlmActor with AlmActorLogging with HasAlmhirtContext ⇒

  def params: HttpHerderServiceFactory.HttpHerderServiceParams
  
  def route = createHerderServiceEndpoint(params)
}