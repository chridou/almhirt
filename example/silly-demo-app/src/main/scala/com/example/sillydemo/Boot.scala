package com.example.sillydemo

import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.context._
import almhirt.context.ComponentFactories
import almhirt.akkax.ComponentFactory
import almhirt.configuration._
import com.example.sillydemo.crazysheep.CrazySheepApp
import com.example.sillydemo.freakydogs.FreakyDogsApp
import almhirt.httpx.spray.marshalling.ContentTypeBoundMarshallerFactory
import riftwarp._
import almhirt.akkax._
import almhirt.http.AlmMediaTypesProvider
import almhirt.httpx.spray.marshalling.DefaultMarshallingInstances
import riftwarp.util.WarpHttpSerializer
import almhirt.httpx.spray.marshalling.MarshallingContentTypesProvider
import almhirt.corex.spray.service.HttpHerderService
import almhirt.context.HasAlmhirtContext
import com.typesafe.config.Config
import almhirt.context.ComponentFactoryBuilderEntry

object Boot {
  def bootContext(system: ActorSystem): AlmFuture[(AlmhirtContext, Stoppable)] = {
    implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
    val rootConfig = system.settings.config
    system.log.info("Boot context")

    for {
      context <- AlmhirtContext(system = system, actorName = None, componentFactories = createComponentFactories())
      stopHttp <- openHttpHerderService(ResolvePath(context.localActorPaths.apps / "herder-service"), context)(system)
    } yield {
      val stop = new Stoppable {
        def stop() {
          stopHttp.stop
          context.stop
        }
      }
      (context, stop)
    }
  }

  private def createComponentFactories(): ComponentFactories = {
    ComponentFactories(
      Seq.empty,
      Seq.empty,
      Seq.empty,
      buildApps = createApps(),
      buildNexus = None)
  }

  private def createApps(): Seq[ComponentFactoryBuilderEntry] = {
    val rw = RiftWarp()
    val problemAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[almhirt.common.Problem]("Problem").withGenericTargets
    val pSerializer = WarpHttpSerializer[Problem](rw)
    val mctp = MarshallingContentTypesProvider[Problem]()
    val problemMarshaller = ContentTypeBoundMarshallerFactory[Problem](mctp, DefaultMarshallingInstances.ProblemMarshallingInst).marshaller(pSerializer)

    val entry1 = { (ctx: AlmhirtContext) =>
      {
        HttpHerderService.paramsFactory(ctx).map(paramsFactory =>
          Props(new {
            override val almhirtContext = ctx
            override val httpHerderServiceParams = paramsFactory(problemMarshaller)
          } with SillyHerderService with Actor with ActorLogging with HasAlmhirtContext {
            def receive = runRoute(route)
            override val actorRefFactory = this.context
          })).map(props => ComponentFactory(props, s"herder-service"))
      }
    }.toCriticalEntry

    val crazySheepFactory = { (ctx: AlmhirtContext) => ComponentFactory(Props(new CrazySheepApp()(ctx)), "crazy-sheep") }.toCriticalEntry
    val freakyDogsFactory = { (ctx: AlmhirtContext) => ComponentFactory(Props(new FreakyDogsApp()(ctx)), "freaky-dogs") }.toCriticalEntry

    Seq(entry1, crazySheepFactory, freakyDogsFactory)
  }

  private def openHttpHerderService(serviceToResolve: ToResolve, ctx: AlmhirtContext)(implicit system: ActorSystem): AlmFuture[Stoppable] = {
    import akka.io.IO
    import spray.can.Http
    import almhirt.almfuture.all._
    implicit val execCtx = system.dispatchers.defaultGlobalDispatcher
    val resolveSettings = ResolveSettings.default
    for {
      (interface, port) <- AlmFuture.completed {
        for {
          section <- system.settings.config.v[Config]("silly-app")
          port <- section.v[Int]("port")
          interface <- section.v[String]("interface")
        } yield (interface, port)
      }
      generator <- AlmFuture.successful {
        val generatorProps = Props(
          new Actor with ActorLogging {
            var stakeholder: ActorRef = null
            def receive: Receive = {
              case "resolve" =>
                context.resolveSingle(serviceToResolve, resolveSettings, None, Some("herder-service-resolver"))
                stakeholder = sender()

              case ActorMessages.ResolvedSingle(httpServiceActor, _) =>
                log.info("Found http service actor.")
                stakeholder ! httpServiceActor
                context.stop(self)

              case ActorMessages.SingleNotResolved(problem, _) =>
                log.error(s"\n\n\nThere will be no herder service!\n\n\n\nCould not find HTTP service @ ${serviceToResolve}:\n$problem")
                context.stop(self)
            }
          })
        system.actorOf(generatorProps, s"herder-service-generator")
      }
      serviceActor <- (generator ? "resolve")(30.seconds).mapCastTo[ActorRef]
      stopHttp <- AlmFuture.successful {
        IO(Http) ! Http.Bind(serviceActor, interface = interface, port = port)
      }
    } yield (new Stoppable { def stop() = { IO(Http) ! Http.Unbind } })
  }
}