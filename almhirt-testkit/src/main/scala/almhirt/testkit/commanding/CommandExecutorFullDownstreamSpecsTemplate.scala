package almhirt.testkit.commanding

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.concurrent.duration.FiniteDuration
import akka.testkit.TestProbe
import almhirt.testkit._
import scala.concurrent.Await
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.core.types._
import almhirt.almvalidation.kit._
import almhirt.core.HasAlmhirt
import almhirt.domain.impl.AggregateRootRepositoryImpl
import almhirt.components.AggregateRootRepositoryRegistry
import almhirt.messaging.MessagePublisher
import almhirt.commanding.impl.CommandExecutorImpl
import almhirt.commanding._

abstract class CommandExecutorFullDownstreamSpecsTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with HasAlmhirt
  with CreatesCellSourceForTestAggregateRoots
  with FunSpec
  with ShouldMatchers { self: CreatesDomainEventLog =>

  def createRepositoryRegistry(testId: Int, cellSource: ActorRef): (AggregateRootRepositoryRegistry, () => Unit) = {
    val repo1 = this.system.actorOf(Props(new AggregateRootRepositoryImpl[AR1, AR1Event](theAlmhirt, cellSource, defaultDuration, defaultDuration)), "AR1Repo_" + testId.toString)
    val repo2 = this.system.actorOf(Props(new AggregateRootRepositoryImpl[AR2, AR2Event](theAlmhirt, cellSource, defaultDuration, defaultDuration)), "AR2Repo_" + testId.toString)
    val registry = AggregateRootRepositoryRegistry()
    registry.register(classOf[AR1], repo1)
    registry.register(classOf[AR2], repo2)
    (registry, () => { this.system.stop(repo1); this.system.stop(repo2) })
  }

  def createCommandExecutor(testId: Int, repositories: AggregateRootRepositoryRegistry): (ActorRef, TestProbe) = {
    val probe = TestProbe()
    val handlers = AR2.Commanding.addCommands(AR1.Commanding.addCommands(CommandHandlerRegistry()))
    val publisher = MessagePublisher.sendToActor(probe.ref)
    (this.system.actorOf(Props(new CommandExecutorImpl(handlers, repositories, publisher, theAlmhirt, FiniteDuration(5, "s"), FiniteDuration(5, "s"), 1000L)), "CommandExecutor_" + testId), probe)
  }

  def useExecutorWithEventLog[T](f: (ActorRef, ActorRef, TestProbe) => T): T = {
    val testId = nextTestId
    val (eventlog, eventLogCleanUp) = createDomainEventLog(testId)
    val cellSource = createCellSource(testId, eventlog)
    val (repoRegistry, closeRepos) = createRepositoryRegistry(testId, cellSource)
    val (executor, publishToProbeprobe) = createCommandExecutor(testId, repoRegistry)
    val close = () => { system.stop(executor); closeRepos(); system.stop(cellSource); system.stop(eventlog); system.stop(publishToProbeprobe.ref); eventLogCleanUp() }
    try {
      val res = f(executor, eventlog, publishToProbeprobe)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }

  describe("CommandExecutor") {
    it("should be creatable") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          true should be(true)
      }
    }
    it("should receive a AR1ComCreateAR1 and acknowledge it with a CommandReceived") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val theCommand = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
          executor ! theCommand
          val res = probe.fishForMessage(defaultDuration, "I'm fishing for CommandReceived") {
            case Message(_, CommandReceived(_, cmd)) => cmd == theCommand
            case x => false
          }
      }
    }
    it("should receive a AR1ComCreateAR1 and acknowledge execution with a CommandExecuted") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val theCommand = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
          executor ! theCommand
          val res = probe.fishForMessage(defaultDuration, "I'm fishing for CommandExecuted") {
            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theCommand.commandId
            case x => false
          }
      }
    }
    it("should receive a AR1ComCreateAR1 and then modify the ar and acknowledge both with a CommandExecuted") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val theFirstCommand = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")

          executor ! theFirstCommand
          probe.fishForMessage(defaultDuration, "I'm fishing for the first CommandExecuted") {
            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theFirstCommand.commandId
            case x => false
          }
          val theSecondCommand = AR1ComChangeB(DomainCommandHeader(theFirstCommand.targettedAggregateRootRef.inc), Some("B"))
          executor ! theSecondCommand
          probe.fishForMessage(defaultDuration, "I'm fishing for the second CommandExecuted") {
            case Message(_, CommandExecuted(_, cmdId)) => cmdId == theSecondCommand.commandId
            case x => false
          }
      }
    }
    it("should signal a CommandNotExecuted when mutating a non existing aggregate root") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val theCommand = AR1ComChangeB(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), Some("B"))
          executor ! theCommand
          probe.fishForMessage(defaultDuration, "I'm fishing for CommandNotExecuted") {
            case Message(_, CommandNotExecuted(_, cmdId, problem)) => cmdId == theCommand.commandId
            case x => false
          }
      }
    }

    it("should emit CommandReceived for each for each command in a sequence") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
          val theCommands =
            List[AR1Command](
              AR1ComCreateAR1(DomainCommandHeader(aggRef), "a"),
              AR1ComChangeB(DomainCommandHeader(aggRef), Some("B")),
              AR1ComChangeA(DomainCommandHeader(aggRef), "s"))
          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
          groupedCommands.foreach(executor ! _)
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 20) {
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => Some(m.command.commandId)
          }

          res.flatten should equal(theCommands.map(_.commandId))
      }
    }

    it("should execute a command sequence and emit CommandExecuted for each command") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
          val theCommands =
            List[AR1Command](
              AR1ComCreateAR1(DomainCommandHeader(aggRef), "a"),
              AR1ComChangeB(DomainCommandHeader(aggRef), Some("B")),
              AR1ComChangeA(DomainCommandHeader(aggRef), "s"))
          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
          groupedCommands.foreach(executor ! _)
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 6) {
            case Message(_, m @ CommandExecuted(_, cmdId)) => Some(cmdId)
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }

          res.flatten should equal(theCommands.map(_.commandId))
      }
    }

    it("should execute a command sequence and emit CommandExecuted for each command in the sequence in the sequence index order even though the commands are submitted in reversed order") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
          val theCommands =
            List[AR1Command](
              AR1ComCreateAR1(DomainCommandHeader(aggRef), "a"),
              AR1ComChangeB(DomainCommandHeader(aggRef), Some("B")),
              AR1ComChangeA(DomainCommandHeader(aggRef), "s"))
          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands).reverse
          groupedCommands.foreach(executor ! _)
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 6) {
            case Message(_, m @ CommandExecuted(_, cmdId)) => Some(cmdId)
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }

          res.flatten should equal(theCommands.map(_.commandId))
      }
    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionSuccessful(3) when tracking is enabled on a creating command") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val trackId = "track me"
          val theCommand = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(3)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionSuccessful(3) when tracking is enabled on a mutating command") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          import almhirt.domaineventlog.DomainEventLog._
          val (initialState, initialEvents) = AR1.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
          Await.result((eventLog ? CommitDomainEvents(initialEvents))(defaultDuration), defaultDuration)
          val trackId = "track me"
          val theCommand = AR1ComChangeA(DomainCommandHeader(initialState.ref), "b").track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(3)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionFailed(3) when tracking is enabled but the creating command is invalid") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val trackId = "track me"
          val theCommand = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid, 10L)), "a").track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(-1)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionFailed(3) when tracking is enabled but the mutating command is invalid") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val trackId = "track me"
          val theCommand = AR1ComChangeA(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(-1)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }
    it("should emit ExecutionStarted(1) ->  ExecutionFailed(3) when tracking is enabled but the command is unregistered") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val trackId = "track me"
          val theCommand = AR1ComUnregisteredCommand(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid))).track(trackId)
          executor ! theCommand
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(-1)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 3 :: Nil)
      }
    }
    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionSuccessful(3) when tracking is enabled on the first command of a sequence") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
          val trackId = "track me"
          val theCommands =
            List[AR1Command](
              AR1ComCreateAR1(DomainCommandHeader(aggRef), "a").track("track me"),
              AR1ComChangeB(DomainCommandHeader(aggRef), Some("B")),
              AR1ComChangeA(DomainCommandHeader(aggRef), "s"))
          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
          groupedCommands.foreach(executor ! _)
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionSuccessful)) if (m.trackId == trackId) => Some(3)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }

    it("should emit ExecutionStarted(1) -> ExecutionInProcess(2) -> ExecutionFailed(3) when tracking is enabled on the first command of a sequence but one command of the sequence sets an invalid value") {
      useExecutorWithEventLog {
        (executor, eventLog, probe) =>
          val aggRef = AggregateRootRef(theAlmhirt.getUuid)
          val trackId = "track me"
          val theCommands =
            List[AR1Command](
              AR1ComCreateAR1(DomainCommandHeader(aggRef), "a").track("track me"),
              AR1ComChangeB(DomainCommandHeader(aggRef), Some("")),
              AR1ComChangeA(DomainCommandHeader(aggRef), "s"))
          val groupedCommands = CommandGrouping.groupCommands("mygroup", theCommands)
          groupedCommands.foreach(executor ! _)
          val res = probe.receiveWhile(defaultDuration, defaultDuration, 9) {
            case Message(_, ExecutionStateChanged(_, m: ExecutionStarted)) if (m.trackId == trackId) => Some(1)
            case Message(_, ExecutionStateChanged(_, m: ExecutionInProcess)) if (m.trackId == trackId) => Some(2)
            case Message(_, ExecutionStateChanged(_, m: ExecutionFailed)) if (m.trackId == trackId) => Some(3)
            case Message(_, m: CommandExecuted) => None
            case Message(_, m: CommandNotExecuted) => None
            case Message(_, m: CommandReceived) => None
          }.flatten
          res should equal(1 :: 2 :: 3 :: Nil)
      }
    }

  }
}