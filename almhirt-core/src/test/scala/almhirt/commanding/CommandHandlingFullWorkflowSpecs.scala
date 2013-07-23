package almhirt.commanding

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit._
import almhirt.testing.TestConfigs
import java.util.{ UUID => JUUID }
import scala.concurrent.duration._
import scala.concurrent.Await
import scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.Almhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog
import almhirt.domain._
import almhirt.domain.impl.AggregateRootCellImpl
import scala.concurrent.Future
import almhirt.components.AggregateRootRepositoryRegistry
import almhirt.domain.impl.AggregateRootRepositoryImpl
import almhirt.commanding.impl.CommandExecutorImpl
import almhirt.messaging.MessagePublisher

class CommandHandlingFullWorkflowSpecs extends TestKit(ActorSystem("CommandHandlingFullWorkflowSpecsSystem", TestConfigs.default)) with FunSpec with ShouldMatchers {
  import almhirt.domain.caching.impl.AggregateRootCellSourceImpl
  import almhirt.domain.caching.AggregateRootCellSource._
  import almhirt.domain.DomainMessages._
  import almhirt.domain.AggregateRootCell._

  val almhirtAndHandle = Almhirt.notFromConfig(this.system).awaitResult(FiniteDuration(5, "s")).forceResult

  implicit val theAlmhirt = almhirtAndHandle._1
  implicit val defaultWaitDuration = FiniteDuration(200, "ms")
  implicit val futuresContext = theAlmhirt.futuresExecutor

  val nextSpecId = new java.util.concurrent.atomic.AtomicInteger(1)

  val commandHandlerRegistry = AnotherTestArCommanding.Handlers.addCommands(TestArCommanding.Handlers.addCommands(CommandHandlerRegistry()))

  def createEventLog(specId: Int): ActorRef =
    this.system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + specId.toString)

  def createCellSource(specId: Int, eventLog: ActorRef): ActorRef = {
    val propsFactories: Map[Class[_], (JUUID, () => Unit) => Props] =
      Map(
        (classOf[TestAr], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[TestAr, TestArEvent](arId, TestAr.rebuildFromHistory, eventLog, notifyDoesNotExist))),
        (classOf[AnotherTestAr], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AnotherTestAr, AnotherTestArEvent](arId, AnotherTestAr.rebuildFromHistory, eventLog, notifyDoesNotExist))))
    val props = Props(new AggregateRootCellSourceImpl(propsFactories.lift))
    this.system.actorOf(props, "CellSource_" + specId.toString())
  }

  def createRepositoryRegistry(specId: Int, cellSource: ActorRef): (AggregateRootRepositoryRegistry, Vector[ActorRef]) = {
    val testArRepo = this.system.actorOf(Props(new AggregateRootRepositoryImpl[TestAr, TestArEvent](theAlmhirt, cellSource, defaultWaitDuration, defaultWaitDuration)), "TestArRepo_" + specId.toString)
    val anotherTestArRepo = this.system.actorOf(Props(new AggregateRootRepositoryImpl[AnotherTestAr, AnotherTestArEvent](theAlmhirt, cellSource, defaultWaitDuration, defaultWaitDuration)), "AnotherTestArRepo_" + specId.toString)
    val registry = AggregateRootRepositoryRegistry()
    registry.register(classOf[TestAr], testArRepo)
    registry.register(classOf[AnotherTestAr], anotherTestArRepo)
    (registry, Vector(testArRepo, anotherTestArRepo))
  }

  // executor, publishToProbe
  def createCommandExecutor(specId: Int, handlers: CommandHandlerRegistry, repositories: AggregateRootRepositoryRegistry): (ActorRef, TestProbe) = {
    val probe = TestProbe()
    val publisher = MessagePublisher.sendToActor(probe.ref)
    (this.system.actorOf(Props(new CommandExecutorImpl(handlers, repositories, publisher, theAlmhirt)), "CommandExecutor_" + specId.toString), probe)
  }

  // commandExecutor, EventLog, testProbe
  def createTestRig(): (ActorRef, ActorRef, TestProbe, () => Unit) = {
    val specId = nextSpecId.getAndIncrement()
    val eventLog = createEventLog(specId)
    val cellSource = createCellSource(specId, eventLog)
    val (repositorryRegistry, repositories) = createRepositoryRegistry(specId, cellSource)
    val (commandExecutor, probe) = createCommandExecutor(specId, commandHandlerRegistry, repositorryRegistry)

    (commandExecutor, eventLog, probe, () => { system.stop(commandExecutor); repositories.foreach(system.stop(_)); system.stop(cellSource); system.stop(eventLog) })
  }

  def withTestRig[T](f: (ActorRef, ActorRef, TestProbe) => T): T = {
    val (executor, eventLog, probe, close) = createTestRig()
    try {
      val res = f(executor, eventLog, probe)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }

  describe("CommandExecutor") {
//    it("should be creatable") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          true should be(true)
//      }
//    }
//    it("should receive a CreateTestAr and acknowledge it with a CommandReceived") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val theCommand = TestArCommanding.CreateTestAr(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
//          executor ! theCommand
//          val res = probe.fishForMessage(defaultWaitDuration, "I'm fishing for CommandReceived") {
//            case Message(_, CommandReceived(_, cmd)) => cmd == theCommand
//            case x => false
//          }
//      }
//    }
//    it("should receive a CreateTestAr and acknowledge execution with a CommandExecuted") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val theCommand = TestArCommanding.CreateTestAr(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
//          executor ! theCommand
//          val res = probe.fishForMessage(defaultWaitDuration, "I'm fishing for CommandExecuted") {
//            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theCommand.commandId
//            case x => false
//          }
//      }
//    }
//    it("should receive a CreateTestAr and then modify the ar and acknowledge both with a CommandExecuted") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val theFirstCommand = TestArCommanding.CreateTestAr(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
//          executor ! theFirstCommand
//          probe.fishForMessage(defaultWaitDuration, "I'm fishing for the first CommandExecuted") {
//            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theFirstCommand.commandId
//            case x => false
//          }
//          val theSecondCommand = TestArCommanding.ChangeB(DomainCommandHeader(theFirstCommand.targettedAggregateRootRef.inc), Some("B"))
//          executor ! theSecondCommand
//          probe.fishForMessage(defaultWaitDuration, "I'm fishing for the second CommandExecuted") {
//            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theSecondCommand.commandId
//            case x => false
//          }
//      }
//    }
//    it("should signal a CommandNotExecuted when mutating a non existing aggregate root") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val theCommand = TestArCommanding.ChangeB(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), Some("B"))
//          executor ! theCommand
//          probe.fishForMessage(defaultWaitDuration, "I'm fishing for CommandNotExecuted") {
//            case Message(_, CommandNotExecuted(_, cmdId, problem)) => cmdId == theCommand.commandId
//            case x => false
//          }
//      }
//    }
//
//    it("should emit CommandReceived for each for each command in a sequence") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
//          val theCommands =
//            List[TestArCommand](
//              TestArCommanding.CreateTestAr(DomainCommandHeader(aggRef), "a"),
//              TestArCommanding.ChangeB(DomainCommandHeader(aggRef), Some("B")),
//              TestArCommanding.ChangeA(DomainCommandHeader(aggRef), "s"))
//          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
//          groupedCommands.foreach(executor ! _)
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 20) {
//            case Message(_, m: CommandExecuted) => None
//            case Message(_, m: CommandNotExecuted) => None
//            case Message(_, m: CommandReceived) => Some(m.command.commandId)
//          }
//
//          res.flatten should equal(theCommands.map(_.commandId))
//      }
//    }
//
//    it("should execute a command sequence and emit CommandExecuted for each command") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
//          val theCommands =
//            List[TestArCommand](
//              TestArCommanding.CreateTestAr(DomainCommandHeader(aggRef), "a"),
//              TestArCommanding.ChangeB(DomainCommandHeader(aggRef), Some("B")),
//              TestArCommanding.ChangeA(DomainCommandHeader(aggRef), "s"))
//          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
//          groupedCommands.foreach(executor ! _)
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 6) {
//            case Message(_, m @ CommandExecuted(_, cmdId)) => Some(cmdId)
//            case Message(_, m: CommandNotExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }
//
//          res.flatten should equal(theCommands.map(_.commandId))
//      }
//    }
//
//    it("should execute a command sequence and emit CommandExecuted for each command in the sequence in the sequence index order even though the commands are submitted in reversed order") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
//          val theCommands =
//            List[TestArCommand](
//              TestArCommanding.CreateTestAr(DomainCommandHeader(aggRef), "a"),
//              TestArCommanding.ChangeB(DomainCommandHeader(aggRef), Some("B")),
//              TestArCommanding.ChangeA(DomainCommandHeader(aggRef), "s"))
//          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands).reverse
//          groupedCommands.foreach(executor ! _)
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 6) {
//            case Message(_, m @ CommandExecuted(_, cmdId)) => Some(cmdId)
//            case Message(_, m: CommandNotExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }
//
//          res.flatten should equal(theCommands.map(_.commandId))
//      }
//    }
//    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionSuccessful(3) when tracking is enabled on a creating command") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val trackId = "track me"
//          val theCommand = TestArCommanding.CreateTestAr(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track(trackId)
//          executor ! theCommand
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 9) {
//            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess))if (m.trackId == trackId) => Some(2)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(3)
//            case Message(_, m: CommandExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }.flatten
//          res should equal(1 :: 2 :: 3 :: Nil)
//      }
//    }
//    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionSuccessful(3) when tracking is enabled on a mutating command") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          import almhirt.domaineventlog.DomainEventLog._
//          val (initialState, initialEvents) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
//          Await.result((eventLog ? CommitDomainEvents(initialEvents))(defaultWaitDuration), defaultWaitDuration)
//          val trackId = "track me"
//          val theCommand = TestArCommanding.ChangeA(DomainCommandHeader(initialState.ref), "b").track(trackId)
//          executor ! theCommand
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 9) {
//            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess))if (m.trackId == trackId) => Some(2)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(3)
//            case Message(_, m: CommandExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }.flatten
//          res should equal(1 :: 2 :: 3 :: Nil)
//      }
//    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionFailed(3) when tracking is enabled but the creating command is invalid") {
      withTestRig {
        (executor, eventLog, probe) =>
          val trackId = "track me"
          val theCommand = TestArCommanding.CreateTestAr(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid, 10L)), "a").track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess))if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }
//    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionFailed(3) when tracking is enabled but the mutating command is invalid") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val trackId = "track me"
//          val theCommand = TestArCommanding.ChangeA(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track(trackId)
//          executor ! theCommand
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 9) {
//            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess))if (m.trackId == trackId) => Some(2)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
//            case Message(_, m: CommandExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }.flatten
//          res should equal(1 :: 2 :: 3 :: Nil)
//      }
//    }
//    it("should emit ExecutionStarted(1) ->  ExecutionFailed(3) when tracking is enabled but the command is unregistered") {
//      withTestRig {
//        (executor, eventLog, probe) =>
//          val trackId = "track me"
//          val theCommand = TestArCommanding.UnregisteredTestArCommand(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid))).track(trackId)
//          executor ! theCommand
//          val res = probe.receiveWhile(defaultWaitDuration, defaultWaitDuration, 9) {
//            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
//            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
//            case Message(_, m: CommandExecuted) => None
//            case Message(_, m: CommandReceived) => None
//          }.flatten
//          res should equal(1 :: 3 :: Nil)
//      }
//    }

  }
}